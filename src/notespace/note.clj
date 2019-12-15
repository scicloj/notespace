(ns notespace.note
  (:require [clojure.string :as string]
            [hiccup.core :as hiccup]
            [hiccup.element :as element]
            [hiccup.page :as page]
            [notespace.util :refer [deref-if-ideref careful-zprint fmap only-one pprint-and-return]]
            [clojure.pprint :as pp]
            [rewrite-clj.node]
            [clojure.java.io :as io]
            [clojure.java.browse :refer [browse-url]]
            [zprint.core :as zp]
            [clojure.java.shell :refer [sh]]
            [markdown.core :refer [md-to-html-string]]
            [clojure.walk :as walk]
            [cljfmt.core]
            [cljfmt.main])
  (:import java.io.File
           clojure.lang.IDeref))

(def ns->config (atom {}))

(defn config-this-ns! [conf]
  (swap! ns->config assoc *ns* conf))

;; A note has a kind, a collection of forms, a return value, a rendered result, and a status.
(defrecord Note [kind forms value rendered status])

;; A note's kind controls various parameters of its evaluation and rendering.
(def kind->behaviour
  (atom {}))

;; A note expression begins with one of several note symbols,
;; that have corresponding note kinds.
;; E.g., en expression of the form (note-md ...) is a note expression
;; of kind :md.
(def note-symbol->kind
  (atom {}))


;; We have a catalogue of notes, holding a sequence of notes per namespace.
(def ns->notes (atom {}))

;; We can also find a given note's index in the sequence.
;; To do that, we assume and make sure that any given combinnation of kind and forms appears only once.
(def ns->kind-and-forms->idx (atom {}))

;; We also keep track of changes in source files corresponding to namespaces.
(def ns->last-modification (atom {}))

(defn ns->src-filename [namespace]
  (let [base-path (-> namespace
                      (@ns->config)
                      :base-path
                      (or "src/"))]
    (str base-path
         (-> namespace
             str
             (string/replace "." "/")
             (string/replace "-" "_"))
         ".clj")))

(defn src-file-modified? [namespace]
  (let [previous-modifiction-time (@ns->last-modification namespace)
        modification-time (-> *ns* ns->src-filename io/file (.lastModified))]
    (swap! ns->last-modification assoc namespace modification-time)
    (not= previous-modifiction-time modification-time)))

;; We can collect all expressions in a namespace.
(defn ns-expressions [namespace]
  (->> namespace
       ns->src-filename
       slurp
       (format "[%s]")
       read-string))

;; Each note expression can be converted to a note.
(defn kind-and-forms->Note [kind forms]
  (->Note kind
          (vec forms)
          nil
          nil
          {}))

(defn expr->Note
  ([expr]
   (when (sequential? expr)
     (when-let [kind (-> expr first (@note-symbol->kind))]
       (let [[& forms] (rest expr)]
         (kind-and-forms->Note kind forms))))))

;; Thus we can collect all notes in a namespace.
(defn ns-notes [namespace]
  (->> namespace
       ns-expressions
       (map expr->Note)
       (filter some?)))

;; We can get the updated notes of a namespace.
;; We try not to update things that have not changed.
(defn updated-notes [namespace]
  (let [old-notes (@ns->notes namespace)
        modified (src-file-modified? namespace)]
    {:modified modified
     :notes (if (not modified)
              old-notes
              (let [new-notes (ns-notes *ns*)]
                (mapv (fn [old-note new-note]
                        (let [change (->> [old-note new-note]
                                          (map (juxt :kind :forms))
                                          (apply =)
                                          not)]
                         (if change
                           (assoc new-note :status :changed)
                           old-note)))
                     (concat old-notes (repeat nil))
                     new-notes)))}))

;; We can update our memory regarding the notes in the namespace.
(defn update-notes! [namespace]
  (let [{:keys [modified notes]} (updated-notes namespace)]
    (when modified
      (let [kind-and-forms->idx (->> notes
                                     (map-indexed (fn [idx note]
                                                    {:idx  idx
                                                     :kind-and-forms (-> note ((juxt :kind :forms)))}))
                                     (group-by :kind-and-forms)
                                     (fmap (comp :idx only-one)))]
        (swap! ns->notes assoc namespace notes)
        (swap! ns->kind-and-forms->idx assoc namespace kind-and-forms->idx)))
    notes))

;; When a note of a certain kind is evaluated,
;; itw forms are evaluated, and the catalogue of notes is updated.
(defn note-of-kind [kind forms]
  (update-notes! *ns*)
  (let [value (eval (cons 'do forms))]
    (if-let [idx (get-in @ns->kind-and-forms->idx
                         [*ns* [kind forms]])]
      (do (swap! ns->notes assoc-in [*ns* idx :value]
                 value)
          (get-in @ns->notes [*ns* idx]))
      (do (println [:note-not-found-in-ns :did-you-save?])
          (assoc (kind-and-forms->Note kind forms)
                 :value value)))))

;; A specific note kind is defined by:
;; - defining their behaviour
;; - connecting a dedicated note-symbol
;; - assigning to that symbol a macro that wraps note-of-kind

(defmacro defkind [note-symbol kind behaviour]
  (swap! kind->behaviour assoc kind (eval behaviour))
  (swap! note-symbol->kind assoc note-symbol kind)
  `(defmacro ~note-symbol [& forms#]
     (list 'note-of-kind ~kind (list 'quote forms#))))

;; Now let us define several built-in kinds:

(declare value->html)
(defkind note
  :code {:render-src?    true
         :value-renderer #'value->html})

(defn md->html [md]
  [:div
   (md-to-html-string md)])

(defkind note-md
  :md   {:render-src?    false
         :value-renderer md->html})

(defkind note-as-md
  :as-md   {:render-src?    true
            :value-renderer md->html})

(defkind note-hiccup
  :hiccup {:render-src? false
           :value-renderer (fn [h] (hiccup/html h))})

(defkind note-as-hiccup
  :as-hiccup {:render-src?    true
           :value-renderer (fn [h] (hiccup/html h))})

(defkind note-void
  :void {:render-src?    true
         :value-renderer (constantly nil)})


;; A note is rendered in the following way:
;; If its value is an IDeref, then the it is dereferenced.
;; Otherwise, its value is taken as-is.
;; The rendered value is saved.


(defn form->html [form print-fn]
  [:code {:class "prettyprint lang-clj"}
   (-> form
       print-fn
       with-out-str
       (string/replace #"\n" "</br>")
       (string/replace #" " "&nbsp;"))])

(defn value->html [v]
  (cond (fn? v) ""
        (sequential? v) (case (first v)
                          :hiccup (hiccup/html v)
                          (form->html v pp/pprint))
        :else   (form->html v pp/pprint)))

(defn render! [anote]
  (pp/pprint (select-keys anote [:kind :forms]))
  (let [renderer (-> anote :kind (@kind->behaviour) :value-renderer)
        rendered (-> anote
                     :value
                     deref-if-ideref
                     renderer)]
    (if-let [idx      (get-in @ns->kind-and-forms->idx
                             [*ns*
                              (-> anote ((juxt :kind :forms)))])]
      (let [path [*ns* idx]]
        (swap! ns->notes update-in path
               #(merge %
                       {:rendered rendered
                        :status   :fresh}))
        (get-in @ns->notes path))
      (assoc anote :rendered rendered))))

;; Any namespace has a corresponding output html file.
(defn ns->out-filename [namespace]
  (let [filename  (-> namespace
                      str
                      (string/replace "." "/")
                      (->> (format "resources/public/%s/index.html")))
        dir (-> filename
                (File.)
                (.getParentFile))]
    (when-not (.exists dir)
      (.mkdirs dir))
    filename))

;; We can render the notes of a namespace to the file.
(defn note->hiccup [anote]
  [:p
   (when (-> anote :kind (@kind->behaviour) :render-src?)
     (->> anote
          :forms
          (map (fn [form]
                 [:div
                  (-> form
                      (form->html #(careful-zprint % 40)))]))
          (into [:div
                 {:style "background-color:#f2f2f2; width: 100%"}])
          (vector :p)))
   (:rendered anote)])

(defn js-deps []
  ["https://cdn.jsdelivr.net/gh/google/code-prettify@master/loader/run_prettify.js"
   "https://cdnjs.cloudflare.com/ajax/libs/prettify/r298/lang-clj.js"])

(defn footer []
  (let [origin-url (-> (sh "git" "remote" "get-url" "origin")
                       :out)

        repo (when (seq origin-url)
               (string/replace origin-url #"git@github.com:|.git" ""))
        repo-url (some->> repo
                          (str "https://github.com/"))
        ns-url (some-> repo-url
                       (str
                        "/tree/master/"
                        (ns->src-filename *ns*)))]
    [:div
     [:hr]
     [:i
      [:small
       ;; "gh: "
       ;; [:a {:href repo-url} repo]
       ;; [:br]
       ;; "ns:  "
       (if ns-url
         [:a {:href ns-url} *ns*]
         *ns*)
       " - created by " [:a {:href "https://github.com/scicloj/notespace"}
                       "notespace"] ", " (java.util.Date.) "."]]]))

(defn render-notes!
  [notes & {:keys [file]
            :or   {file (str (File/createTempFile "rendered" ".html"))}}]
  (->> notes
       (map render!)
       (map note->hiccup)
       (#(concat
          %
          [(footer)]))
       (into [:div])
       (vector :body
               {:style "background-color:#dddddd;"}
               (->> (js-deps)
                    (map page/include-js)
                    (into [:head])))
       hiccup/html
       page/html5
       (spit file))
  (println [:wrote file])
  file)

(defn render-ns! [namespace]
  (-> namespace
      (@ns->notes)
      (render-notes!
       :file (ns->out-filename namespace))))

(defn render-this-ns! []
  (render-ns! *ns*))


;; Printing a note results in rendering it,
;; and showing the rendered value in the browser.

(defn print-note [anote]
  (let [file (render-notes! [anote])]
    (browse-url file)))

;; Overriding print
(defmethod print-method Note [anote _]
  (print-note anote))

;; Overriding pprint
(defmethod pp/simple-dispatch Note [anote]
  (print-note anote))

;; Why is this necessary?
(defmethod print-dup Note [anote _]
  (print-note anote))


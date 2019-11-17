(ns notespace.note
  (:require [clojure.string :as string]
            [hiccup.core :as hiccup]
            [hiccup.element :as element]
            [hiccup.page :as page]
            [notespace.util :refer [fmap only-one pprint-and-return]]
            [clojure.pprint :as pp]
            [cljfmt.core]
            [rewrite-clj.node]
            [clojure.java.io :as io]
            [clojure.java.browse :refer [browse-url]]
            [zprint.core :as zp]
            [clojure.java.shell :refer [sh]]
            [markdown.core :refer [md-to-html-string]])
  (:import java.io.File
           clojure.lang.IDeref))

(def ns->config (atom {}))

(defn config-this-ns! [conf]
  (swap! ns->config assoc *ns* conf))

;; A note has a kind, a collection of forms, a return value, a rendered result, and a status.
(defrecord Note [kind forms value rendered status])

;; A note's kind controls various parameters of its evaluation and rendering.
(declare value->html)

(def kind->behaviour
  (atom
   {:code {:render-src?    true
           :value-renderer #'value->html}
    :md   {:render-src?    false
           :value-renderer md-to-html-string}
    :void {:render-src?    true
           :value-renderer (constantly nil)}}))

;; We have a catalogue of notes, holding a sequence of notes per namespace.
(def ns->notes (atom {}))

;; We can also find a note's location in the sequence by its forms.
;; To do that, we assume and make sure that no two notes have the same forms.
(def ns->forms->note-idx (atom {}))

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

;; A note expression begins with one of several note symbols,
;; that have corresponding note kinds.
;; E.g., en expression of the form (note-md ...) is a note expression
;; of kind :md.
(def note-symbol->kind
  (atom {'note      :code
         'note-md   :md
         'note-void :void}))

;; Each note expression can be converted to a note.
(defn expr->Note
  ([expr]
   (when (sequential? expr)
     (when-let [kind (-> expr first (@note-symbol->kind))]
       (let [[& forms] (rest expr)]
         (->Note kind
                 (vec forms)
                 nil
                 nil
                 {}))))))

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
                       (let [change (not= (:forms old-note)
                                          (:forms new-note))]
                         (if change
                           (assoc new-note :status :changed)
                           old-note)))
                     (concat old-notes (repeat nil))
                     new-notes)))}))

;; We can update our memory regarding the notes in the namespace.
(defn update-notes! [namespace]
  (let [{:keys [modified notes]} (updated-notes namespace)]
    (when modified
      (let [forms->note-idx (->> notes
                                 (map-indexed (fn [idx note]
                                                {:idx  idx
                                                 :note note}))
                                 (group-by (comp :forms :note))
                                 (fmap (comp :idx only-one)))]
        (swap! ns->notes assoc namespace notes)
        (swap! ns->forms->note-idx assoc namespace forms->note-idx)))
    notes))

;; Given the forms of a note in a namespace,
;; we can check its location in the sequence of notes.
(defn forms->location [namespace forms]
  (get-in @ns->forms->note-idx [namespace forms]))

;; When a note of a certain kind is evaluated,
;; itw forms are evaluated, and the catalogue of notes is updated.
(defmacro note-kind [kind forms]
  (update-notes! *ns*)
  (let [value (eval (cons 'do forms))
        idx (forms->location *ns* forms)]
    (swap! ns->notes assoc-in [*ns* idx :value]
           value)
    `(get-in @ns->notes [~*ns* ~idx])))

(defmacro note [& forms] `(note-kind :code ~forms))
(defmacro note-md [& forms] `(note-kind :md ~forms))
(defmacro note-void [& forms] `(note-kind :void ~forms))

;; A note is rendered in the following way:
;; If its value is an IDeref, then the it is dereferenced.
;; Otherwise, its value is taken as-is.
;; The rendered value is saved.

(defn deref-if-ideref [v]
  (if (instance? IDeref v)
    @v
    v))

(defn form->html [form print-fn]
  [:code {:class "prettyprint"}
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
  (let [renderer (-> anote :kind (@kind->behaviour) :value-renderer)
        rendered (-> anote
                     :value
                     deref-if-ideref
                     renderer)
        idx      (->> anote :forms (forms->location *ns*))
        path [*ns* idx]]
    (swap! ns->notes update-in path
           #(merge %
                   {:rendered rendered
                    :status   :fresh}))
    (get-in @ns->notes path)))

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
                      (form->html zp/zprint))]))
          (into [:div
                 {:style "background-color:#e6e6e6;"}])
          (vector :p)))
   (:rendered anote)])

(defn js-deps []
  ["https://cdn.jsdelivr.net/gh/google/code-prettify@master/loader/run_prettify.js"])

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
(defmethod print-method Note [anote _]
  (let [file (render-notes! [anote])]
    (future (sh "firefox" file))
    #_(browse-url file)))


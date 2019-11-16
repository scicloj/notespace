(ns notespace.note
  (:require [clojure.string :as string]
            [hiccup.core :as hiccup]
            [hiccup.page :as page]
            [notespace.util :refer [fmap only-one pprint-and-return]]
            [clojure.pprint :as pp]
            [cljfmt.core]
            [rewrite-clj.node]
            [clojure.java.io :as io]
            [clojure.java.browse :refer [browse-url]]
            [zprint.core :as zp]
            [clojure.java.shell :refer [sh]])
  (:import java.io.File
           clojure.lang.IDeref))


;; A note has a kind, a collection of forms, a return value, a rendered result, and a status.
(defrecord Note [kind forms value rendered status])

;; A note's kind controls various parameters of its evaluation and rendering.

(def kind->behaviour
  {:code {:render-src true}
   :md   {:render-src false}})

;; We have a catalogue of notes, holding a sequence of notes per namespace.
(def ns->notes (atom {}))

;; We can also find a note's location in the sequence by its forms.
;; To do that, we assume and make sure that no two notes have the same forms.
(def ns->forms->note-idx (atom {}))

;; We also keep track of changes in source files corresponding to namespaces.
(def ns->last-modification (atom {}))

(defn ns->src-filename [namespace]
  (-> namespace
      str
      (string/replace "." "/")
      (->> (format "src/%s.clj"))))

(defn src-file-modified? [namespace]
  (let [previous-modifiction-time (@ns->last-modification namespace)
        modification-time (-> *ns* ns->src-filename io/file (.lastModified))]
    (swap! ns->last-modification assoc namespace modification-time)
    (not= previous-modifiction-time modification-time)))

;; We can collect all expressions in a namespace.
(defn ns-expressions [namespace]
  (-> namespace
      str
      (string/replace #"\." "/")
      (->> (format "src/%s.clj")
           slurp
           (format "[%s]")
           read-string)))

;; A note expression begins with one of several note symbols,
;; that have corresponding note kinds.
;; E.g., en expression of the form (note-md ...) is a note expression
;; of kind :md.
(def note-symbol->kind #{'note :code
                         'note-md :md})

;; Each note expression can be converted to a note.
(defn expr->Note
  ([expr]
   (when (sequential? expr)
     (when-let [kind (-> expr first note-symbol->kind)]
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
  (println [:forms forms])
  (update-notes! *ns*)
  (let [value (eval (cons 'do forms))
        idx (forms->location *ns* forms)]
    (swap! ns->notes assoc-in [*ns* idx :value]
           value)
    `(get-in @ns->notes [~*ns* ~idx])))

(defmacro note [& forms] `(note-kind :code ~forms))
(defmacro note-md [& forms] `(note-kind :md ~forms))

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
  (let [rendered (-> anote
                     :value
                     deref-if-ideref
                     value->html)
        idx      (->> anote :forms (forms->location *ns*))]
    (swap! ns->notes update-in [*ns* idx]
           #(merge %
                   {:rendered rendered
                    :status   :fresh}))
    rendered))

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
   (->> anote
        :forms
        (map (fn [form]
               [:div
                (-> form
                    (form->html zp/zprint)
 )]))
        (into [:div
               {:style "background-color:#eeeeee;"}])
        (vector :p))
   (:rendered anote)])

(defn js-deps []
  ["https://cdn.jsdelivr.net/gh/google/code-prettify@master/loader/run_prettify.js"])

(defn footer []
  [:div
   [:hr]
   [:small
    [:p
    "Created by " [:a {:href "https://github.com/scicloj/notespace"}
                   "notespace"] ", " (java.util.Date.) "."]
   [:p
    "ns:  " *ns*]
   [:p
    "git: " (let [url (-> (sh "git" "remote" "get-url" "origin")
                          :out)]
              (if (seq url)
                url
                "?"))]]])

(defn render-notes!
  [notes & {:keys [file]
            :or   {file (str (File/createTempFile "rendered" ".html"))}}]
  (doseq [anote notes]
    (render! anote))
  (->> notes
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
  file)

(defn render-ns! [namespace]
  (-> namespace
      (@ns->notes)
      (render-notes!
       :file (ns->out-filename namespace))))

;; ;; We watch the catalogue of notes, and render them when it changes.
;; (add-watch
;;  ns->notes :render
;;  (fn [key reference old-state new-state]
;;    (doseq [[namespace new-notes] new-state]
;;      (render-ns! namespace new-notes))))


;; Printing a note results in rendering it,
;; and showing the rendered value in the browser.
(defmethod print-method Note [anote _]
  (let [file (render-notes! [anote])]
    (future (sh "firefox" file))
    #_(browse-url file)))

;; What to do with :status ?


;; (def kind->behaviour
;;   {:fn  {:eval            true
;;          :naturally-fresh true
;;          :rendered        false}
;;    :out {:eval            false
;;          :naturally-fresh false
;;          :rendered        true}})

;; (defn update-note-change [prev-note note]
;;   (assoc note
;;          :change (not= (:forms prev-note)
;;                        (:forms note))))

;; (defn update-note-fresh [note]
;;   (assoc note
;;          :fresh (or (-> note :kind kind->behaviour :naturally-fresh)
;;                     (and (:fresh prev-note)
;;                          (not changed)))))

;; (defn update-notes-transitive-change [notes]
;;   (map (fn [note transitive-change]
;;          (assoc note :transitive-change transitive-change))
;;        notes
;;        (->> notes
;;             (map :changed)
;;             (reductions #(or %1 %2)))))

;; (defn update-notes [prev-notes notes]
;;   (let [notes1         (map updated-note
;;                             (concat prev-notes
;;                                     (repeat nil))
;;                             notes)
;;         sticky-changes (->> notes1
;;                             (map :changed)
;;                             (reductions #(or %1 %2)))]
;;     (->> notes1
;;          (map (fn [sticky-change note]
;;                 (assoc note :changed sticky-change))
;;               sticky-changes)
;;          (map (fn [note]
;;                 (assoc note
;;                        :fresh (or (-> note :kind kind->behaviour :naturally-fresh)
;;                                   (not (:changed note)))))))))

;; (defn update-ns-notes! []
;;   (swap! ns->notes
;;          update
;;          *ns*
;;          #(update-notes
;;            %
;;            (ns-notes *ns*))))

;; (defn refresh-note! [anote]
;;   (println [:refreshing anote])
;;   (if (-> anote :fresh not)
;;     (do
;;       (println "__________________")
;;       (assoc anote
;;              :fresh true
;;              :value (if  (-> anote :kind kind->behaviour :rendered)
;;                       (->> anote
;;                            :forms
;;                            (map eval))
;;                       (doseq [form (:forms anote)]
;;                         (eval form)))))
;;     anote))

;; (defn refresh-ns! []
;;   (update-ns-notes!)
;;   (-> ns->notes
;;       (swap! update *ns*
;;              (partial mapv refresh-note!))
;;       (get *ns*)))

;; (defn rerender! []
;;   (->> (refresh-ns!)
;;        pprint-and-return
;;        (map (fn [anote]
;;               (into [:div
;;                      (println anote)
;;                      (->> anote
;;                           :forms
;;                           (map (fn [form]
;;                                  [:p
;;                                   [:code {:class "prettyprint"}
;;                                    (->> form
;;                                         pp/pprint
;;                                         with-out-str
;;                                         cljfmt.core/reformat-string)]]))
;;                           (into [:div
;;                                  {:style "background-color:#eeeeee;"}]))
;;                      [:p (if (:fresh anote)
;;                            ""
;;                            "[?]")]]
;;                     (:value anote))))
;;        (into [:div])
;;        (vector :body
;;                {:style "background-color:#cccccc; align:center"})
;;        (vector :head
;;                (page/include-js
;;                 "https://cdn.jsdelivr.net/gh/google/code-prettify@master/loader/run_prettify.js"))
;;        hiccup/html
;;        page/html5
;;        (spit "resources/public/index.html")))



(ns notespace.v2.note
  (:require [clojure.string :as string]
            [hiccup.core :as hiccup]
            [hiccup.page :as page]
            [notespace.v2.reader :as reader]
            [notespace.v2.util :refer [fmap only-one]]
            [notespace.v2.check :as check]
            [notespace.v2.view :as view]
            [notespace.v2.behaviours :refer [kind->behaviour]]
            [clojure.pprint :as pp]
            [rewrite-clj.node]
            [clojure.java.browse :refer [browse-url]]
            [notespace.v2.cdn :as cdn]
            [notespace.v2.js :as js]
            [cambium.core :as log]
            [notespace.v2.source :as source])
  (:import java.io.File
           clojure.lang.IDeref))

;; A note has a kind, possibly a label, a collection of forms, the reader metadata, a return value, a rendered result, and a status.
(defrecord Note [kind label forms metadata value rendered status])

;; A note form begins with one of several note symbols,
;; that have corresponding note kinds.
;; E.g., a form of the form (note-md ...) is a note form
;; of kind :md.
(def note-symbol->kind
  (atom {}))

;; We have a catalogue of notes, holding a sequence of notes per namespace.
(def ns->notes (atom {}))

;; We also keep, for every line of code,
;; the index of the corresponding note in the sequence of notes,
;; if that line happens to lie inside a note.
(def ns->line->index (atom {}))

;; We also keep the indices of every note's label appearances in the sequence.
(def ns->label->indices (atom {}))

;; We can collect all toplevel forms in a namespace,
;; together with the reader metadata.
(defn ->ns-topforms-with-metadata [namespace]
  (->> namespace
       source/ns->source-filename
       reader/file->topforms-with-metadata))

;; When the first form of a note is a keyword,
;; then it would be considered the form's label
(defn forms->label [[first-form & _]]
  (when (keyword? first-form)
    first-form))

;; Each note toplevel form can be converted to a Note.
(defn kind-forms-and-metadata->Note [kind forms metadata]
  (->Note kind
          (forms->label forms)
          (vec forms)
          metadata
          nil
          nil
          {}))

(defn topform-with-metadata->Note
  ([topform-with-metadata]
   (when (sequential? topform-with-metadata)
     (when-let [kind (-> topform-with-metadata first (@note-symbol->kind))]
       (let [[& forms] (rest topform-with-metadata)]
         (kind-forms-and-metadata->Note
          kind
          forms
          (meta topform-with-metadata)))))))

;; Thus we can collect all notes in a namespace.
(defn ns-notes [namespace]
  (->> namespace
       ->ns-topforms-with-metadata
       (map topform-with-metadata->Note)
       (filter some?)))

;; We can update our notes structures by reading the notes of a namespace.
;; We try not to update things that have not changed.
(defn read-notes-seq! [namespace]
  (let [old-notes       (@ns->notes namespace)
        source-modified (source/source-file-modified? namespace)
        needs-update    (or (not old-notes)
                            source-modified)
        notes           (if (not needs-update)
                          old-notes
                          (let [new-notes (ns-notes namespace)]
                            (mapv (fn [old-note new-note]
                                    (let [change (and (->> [old-note new-note]
                                                           (map (comp :source :metadata))
                                                           (apply =)
                                                           not)
                                                      (->> [old-note new-note]
                                                           (map (juxt :kind :forms))
                                                           (apply =)
                                                           not))]
                                      (if change
                                        (assoc new-note :status :changed)
                                        old-note)))
                                  (concat old-notes (repeat nil))
                                  new-notes)))]
    (when needs-update
      (let [line->index    (->> notes
                                (map-indexed (fn [idx {:keys [metadata]}]
                                               {:idx   idx
                                                :lines (range (:line metadata)
                                                              (-> metadata :end-line inc))}))
                                (mapcat (fn [{:keys [idx lines]}]
                                          (->> lines
                                               (map (fn [line]
                                                      {:idx  idx
                                                       :line line})))))
                                (group-by :line)
                                (fmap (comp :idx only-one)))
            label->indices (->> notes
                                (map-indexed (fn [idx note]
                                               {:idx   idx
                                                :label (:label note)}))
                                (filter :label)
                                (group-by :label)
                                (fmap (partial mapv :idx)))]
        (swap! ns->notes assoc namespace notes)
        (swap! ns->line->index assoc namespace line->index)
        (swap! ns->label->indices assoc namespace label->indices)))
    [:notes (count notes)]))

(defmacro defkind [note-symbol kind behaviour]
  (swap! kind->behaviour assoc kind (eval behaviour))
  (swap! note-symbol->kind assoc note-symbol kind)
  `(defmacro ~note-symbol [& forms#]
     nil))

;; Now let us define several built-in kinds:
(defkind note
  :code {:render-src?    true
         :value-renderer #'view/value->hiccup})

(defkind note-md
  :md   {:render-src?    false
         :value-renderer view/md->hiccup})

(defkind note-as-md
  :as-md   {:render-src?    true
            :value-renderer view/md->hiccup})

(defkind note-hiccup
  :hiccup {:render-src? false
           :value-renderer (fn [h] (hiccup/html h))})

(defkind note-as-hiccup
  :as-hiccup {:render-src?    true
              :value-renderer (fn [h] (hiccup/html h))})

(defkind note-void
  :void {:render-src?    true
         :value-renderer (constantly nil)})

;; We may need support various update transformations for notes.
;; If it has reader metadata, then the catalogue of notes is updated
;; with the resulting note-with-value.
(defn update-note! [namespace transf anote]
  (let [new-note (transf anote)]
    (when-let [m (:metadata anote)]
      (swap! ns->notes
             assoc-in
             [namespace
              (-> m
                  :line
                  ((@ns->line->index namespace)))]
             new-note))
    new-note))

;; A note is computed by evaluating its form to compute its value.
(defn compute-note [anote]
  (let [value (->> anote
                   :forms
                   (cons 'do)
                   eval)
        renderer (-> anote :kind (@kind->behaviour) :value-renderer)
        rendered (renderer value)]
    (assoc anote
           :value value
           :rendered rendered)))

(defn compute-note! [namespace anote]
  (update-note! namespace compute-note anote))

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

(defn render-to-file! [render-fn path]
  (let [path-to-use (or path (str (File/createTempFile "rendered" ".html")))
        html (page/html5 (render-fn))]
    (spit path-to-use html)
  (log/info [::wrote path-to-use])
  html))

(defn render-notes! [namespace notes & {:keys [file]}]
  (render-to-file! (partial view/notes->hiccup namespace notes)
                   file))

(defn render-ns [namespace]
  (hiccup.core/html
   [:html
    {:style "background-color:#fbf8ef;"}
    (into [:head
           (js/mirador-setup)]
          (mapcat cdn/header [:prettify :datatables]))
    [:body
     (if (not namespace)
       "Waiting for a first notespace to appear ..."
       (do (read-notes-seq! namespace)
           (view/notes->hiccup
            namespace
            (@ns->notes namespace))))]]))

(def last-ns-rendered
  (atom nil))

(defn render-ns! [namespace]
  (let [html (render-to-file! (partial render-ns namespace)
                              (ns->out-filename namespace))]
    (reset! last-ns-rendered namespace)
    [:rendered {:ns namespace}]))

(defn render-this-ns []
  (render-ns *ns*))

(defn render-this-ns! []
  (render-ns! *ns*))

(defn check [pred & args]
  [(if (apply pred args)
     :PASSED
     :FAILED)
   (last args)])

(defn compute-note-at-line! [line]
  (read-notes-seq! *ns*)
  (some->> line
           ((@ns->line->index *ns*))
           ((@ns->notes *ns*))
           (compute-note! *ns*))
  [[:computed {:ns   *ns*
               :line line}]
   (render-this-ns!)])

(defn compute-this-notespace! []
  (read-notes-seq! *ns*)
  (->> *ns*
       (@ns->notes)
       (run! (partial compute-note! *ns*)))
  [[:computed {:ns *ns*}]
   (render-this-ns!)])

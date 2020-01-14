(ns notespace.v1.note
  (:require [clojure.string :as string]
            [hiccup.core :as hiccup]
            [hiccup.element :as element]
            [hiccup.page :as page]
            [notespace.v1.repo :as repo]
            [notespace.v1.reader :as reader]
            [notespace.v1.util :refer [deref-if-ideref careful-zprint fmap only-one first-when-one pprint-and-return]]
            [notespace.v1.source :refer [ns->source-filename source-file-modified?]]
            [notespace.v1.hiccup :refer [code->hiccup form->hiccup value->hiccup md->hiccup]]
            [clojure.pprint :as pp]
            [rewrite-clj.node]
            [clojure.java.io :as io]
            [clojure.java.browse :refer [browse-url]]
            [zprint.core :as zp]
            [clojure.java.shell :refer [sh]]
            [clojure.walk :as walk]
            [notespace.v1.cdn :as cdn]
            [cambium.core :as log])
  (:import java.io.File
           clojure.lang.IDeref))

;; A note has a kind, possibly a label, a collection of forms, the reader metadata, a return value, a rendered result, and a status.
(defrecord Note [kind label forms metadata value rendered status])

;; A note's kind controls various parameters of its evaluation and rendering.
(def kind->behaviour
  (atom {}))

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
       ns->source-filename
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

;; We can get the updated notes of a namespace.
;; We try not to update things that have not changed.
(defn updated-notes-seq [namespace]
  (let [old-notes (@ns->notes namespace)
        modified (source-file-modified? namespace)]
    {:modified modified
     :notes    (if (not modified)
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
                         new-notes)))}))

;; We can update our memory regarding the notes in the namespace.
(defn update-notes-seq! [namespace]
  (let [{:keys [modified notes]} (updated-notes-seq namespace)]
    (when modified
      (let [line->index (->> notes
                             (map-indexed (fn [idx {:keys [metadata]}]
                                            {:idx  idx
                                             :lines (range (:line metadata)
                                                           (-> metadata :end-line inc))}))
                             (mapcat (fn [{:keys [idx lines]}]
                                       (->> lines
                                            (map (fn [line]
                                                   {:idx idx
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
    (->> notes
         (mapv (fn [anote]
                 (-> anote
                     :metadata
                     :source
                     read-string
                     eval))))))

(defmacro defkind [note-symbol kind behaviour]
  (swap! kind->behaviour assoc kind (eval behaviour))
  (swap! note-symbol->kind assoc note-symbol kind)
  `(defmacro ~note-symbol [& forms#]
     nil))

;; Now let us define several built-in kinds:

(defkind note
  :code {:render-src?    true
         :value-renderer #'value->hiccup})

(defkind note-md
  :md   {:render-src?    false
         :value-renderer md->hiccup})

(defkind note-as-md
  :as-md   {:render-src?    true
            :value-renderer md->hiccup})

(defkind note-hiccup
  :hiccup {:render-src? false
           :value-renderer (fn [h] (hiccup/html h))})

(defkind note-as-hiccup
  :as-hiccup {:render-src?    true
              :value-renderer (fn [h] (hiccup/html h))})

(defkind note-test
  :test {:render-src? true
         :value-renderer
         (fn [checks]
           (->> checks
                (map (fn [[relation & vals]]
                       (if (apply relation vals)
                         [:p  {:style "color:green"} "PASSED"]
                         [:p  {:style "color:red"} "FAILED"
                          (->> vals
                               (map (fn [v]
                                      [:li (value->hiccup v)]))
                               (into [:ul]))])))
                (into [:div])))})

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
             assoc
             (-> m
                 :line
                 (@ns->line->index))
             new-note))
    new-note))

;; A note is computed by evaluating its form to compute its value.
(defn compute [anote]
  (assoc anote
         :value (->> anote
                     :forms
                     (cons 'do)
                     eval)))

;; A note is rendered in the following way:
;; If its value is an IDeref, then it is dereferenced.
;; Otherwise, its value is taken as-is.
;; The rendered value is saved.
(defn render [anote]
  (let [renderer (-> anote :kind (@kind->behaviour) :value-renderer)
        rendered (-> anote
                     :value
                     deref-if-ideref
                     renderer)]
    (assoc anote
           :rendered rendered)))

(defn interactive-render [line]
  (update-notes-seq! *ns*)
  (some->> line
           ((@ns->line->index *ns*))
           ((@ns->notes *ns*))
           (interactive-update-note! *ns* (comp render compute))
           prn))

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
(defn label->anchor-id [label]
  (->> label name))

(defn label->anchor [label]
  [:a  {;; :style "border: 2px solid green;"
        :id (label->anchor-id label)}
   (format "~~~~%s~~~~" (name label))])

(defn note->hiccup [{:keys [forms label rendered]
                     :as anote}]
  [:p
   (when (-> anote :kind (@kind->behaviour) :render-src?)
     (->> (or [(some-> anote
                       :metadata
                       :source
                       code->hiccup)]
              (->> (if label
                     (rest forms)
                     forms)
                   (map (fn [form]
                          [:div
                           (-> form
                               (form->hiccup #(careful-zprint % 80)))]))))
          (into [:div
                 {:style "background-color:#e8e3f0; width: 100%"}
                 (when label
                   (label->anchor label))])
          (vector :p)))
   rendered])

(defn ns-url [namespace]
  (some-> (repo/repo-url)
          (str
           "/tree/master/"
           (repo/path-relative-to-git-home)
           (ns->source-filename namespace))))

(defn ->reference [namespace]
  [:div
   [:i
    [:small
     (if-let [url (ns-url namespace)]
       [:a {:href url} namespace]
       namespace)
     " - created by " [:a {:href "https://github.com/scicloj/notespace"}
                       "notespace"] ", " (java.util.Date.) "."]]
   [:hr]])

(defn toc [notes]
  (when-let [labels (->> notes
                         (map :label)
                         (filter some?)
                         seq)]
    [:div
     "Table of contents"
     (->> labels
          (map (fn [label]
                 [:li [:a {:href (->> label
                                      label->anchor-id
                                      (str "#"))}
                       (name label)]]))
          (into [:ul]))
     [:hr]]))

(defn ->checks-freqs [notes]
  (when-let [checks-results (->> notes
                                 (map :value)
                                 (filter (fn [v]
                                           (and (vector? v)
                                                (-> v first (#{:FAILED :PASSED})))))
                                 (map first)
                                 seq)]
    (->> checks-results
         frequencies)))

(defn ->checks-summary [checks-freqs]
  (when checks-freqs
    [:div
     "Checks: "
     (->> checks-freqs
          (map (fn [[k n]]
                 (let [color (case k
                               :PASSED "green"
                               :FAILED "red")]
                   [:b {:style (str "color:" color)}
                    n " " (name k) " "])))
          (into [:b]))
     [:hr]]))

(defn render-notes!
  [namespace notes
   & {:keys [file]
      :or   {file (str (File/createTempFile "rendered" ".html"))}}]
  (let [checks-freqs (->checks-freqs notes)
        checks-summary (->checks-summary checks-freqs)
        reference (->reference namespace)]
    (->> [:body
          {:style "background-color:#fbf8ef;"}
          (->> :prettify
               cdn/header
               (into [:head]))
          [:div
           [:h1 (str namespace)]
           reference
           checks-summary
           (toc notes)
           (->> notes
                (map (comp note->hiccup
                           (partial update-note!
                                    namespace (comp render compute)))))
           [:hr]
           checks-summary
           reference]]
         hiccup/html
         page/html5
         (spit file))
    (log/info [::wrote file])
    (when checks-freqs
      (log/info [::checks checks-freqs])))
  file)

(defn render-ns! [namespace]
  (update-notes-seq! namespace)
  (render-notes!
   namespace
   (@ns->notes namespace)
   :file (ns->out-filename namespace)))

(defn render-this-ns! []
  (update-notes-seq! *ns*)
  (render-ns! *ns*))

;; Printing a note results in rendering it,
;; and showing the rendered value in the browser.

(defn print-note [anote]
  (let [file (render-notes! *ns* [anote])]
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





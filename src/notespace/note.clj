(ns notespace.note
  (:require [notespace.reader :as reader]
            [notespace.util :refer [fmap only-one realize]]
            [rewrite-clj.node]
            [notespace.source :as source]
            [notespace.state :as state]
            [clojure.pprint :as pp]
            [cambium.core :as log])
  (:import java.io.File
           clojure.lang.IDeref))

;; A note has a kind, possibly a label, a collection of forms, and the reader metadata.
(defrecord Note [kind label forms metadata])

;; A note's state has a return value, a rendered result, and a status.
(defrecord NoteState [value rendered status])

(defn note->note-state [namespace anote]
  (->> anote
       :metadata
       :line
       (state/ns->line->index namespace)
       (state/ns->note-state namespace)))


;; We can collect all toplevel forms in a namespace,
;; together with the reader metadata.
(defn ->ns-topforms-with-metadata [namespace]
  (->> namespace
       source/ns->source-filename
       reader/file->topforms-with-metadata
       (filter (comp :line meta))))

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
          metadata))

(defn note-topform? [topform]
  (and (sequential? topform)
       (-> topform
           first
           state/note-symbol->kind)))

(defn strings-topform? [topform]
  (and (sequential? topform)
       (-> topform
           first
           string?)))

(defn ns-topform? [topform]
  (and (sequential? topform)
       (-> topform
           first
           (= 'ns))))

(defn metadata->kind [metadata]
  (when metadata
    (->> (state/kind->behaviour)
         keys
         (drop-while (complement metadata))
         first)))

(defn topform-with-metadata->Note
  [topform-with-metadata]
  (let [metadata (meta topform-with-metadata)]
    (cond
      ;;
      (ns-topform? topform-with-metadata)
      nil
      ;;
      (note-topform? topform-with-metadata)
      (when-let [kind (-> topform-with-metadata first state/note-symbol->kind)]
        (let [[& forms] (rest topform-with-metadata)]
          (kind-forms-and-metadata->Note kind
                                         forms
                                         metadata)))
      ;;
      (strings-topform? topform-with-metadata)
      (kind-forms-and-metadata->Note (-> metadata metadata->kind (or :md))
                                     topform-with-metadata
                                     metadata)
      ;;
      :else
      (kind-forms-and-metadata->Note (-> metadata metadata->kind (or :code))
                                     [topform-with-metadata]
                                     metadata))))

;; Thus we can collect all notes in a namespace.
(defn ns-notes [namespace]
  (->> namespace
       ->ns-topforms-with-metadata
       (map topform-with-metadata->Note)
       (filter some?)))

;; We can update our notes structures by reading the notes of a namespace.
;; We try not to update things that have not changed.

;; TODO: Rethink
(defn different-note? [old-note new-note]
  (or (->> [old-note new-note]
           (map (comp :source :metadata))
           (apply not=))
      (->> [old-note new-note]
           (map (juxt :kind :forms))
           (apply not=))))

(defn evaluate-note [anote]
  (try
    (->> anote
         :forms
         (cons 'do)
         eval)
    (catch Exception e
      (throw (ex-info "Note evaluation failed."
                      {:note      anote
                       :exception e}))) ))

(defn initial-note-state [anote]
  (->NoteState
   (evaluate-note anote)
   nil
   {}))

(defn read-notes-seq! [namespace]
  (let [old-notes            (state/ns->notes namespace)
        old-notes-states     (state/ns->note-states namespace)
        old-notes-and-states (map vector
                                  old-notes
                                  old-notes-states)
        source-modified      (source/source-file-modified? namespace)
        needs-update         true #_(or (not old-notes)
                                      source-modified)
        notes-and-states     (if (not needs-update)
                               old-notes-and-states
                               (let [new-notes (ns-notes namespace)]
                                 (mapv (fn [[old-note old-note-state] new-note]
                                         (if true #_(different-note? old-note new-note)
                                           [new-note (initial-note-state new-note)]
                                           [(merge old-note
                                                   (select-keys new-note [:metadata]))
                                            #_old-note-state
                                            (initial-note-state old-note)]))
                                       (concat old-notes-and-states (repeat nil))
                                       new-notes)))
        notes                (map first notes-and-states)]
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
                                (map-indexed (fn [idx anote]
                                               {:idx   idx
                                                :label (:label anote)}))
                                (filter :label)
                                (group-by :label)
                                (fmap (partial mapv :idx)))]
        (state/assoc-in-state!
         [:ns->notes namespace] (mapv first notes-and-states)
         [:ns->note-states namespace] (mapv second notes-and-states)
         [:ns->line->index namespace] line->index
         [:ns->label->indices namespace] label->indices)))
    [:notes
     notes-and-states
     (if needs-update :updated :not-updated)
     (count notes)]))

;; We support various update transformations for notes' states.
(defn update-note-state! [namespace transf anote]
  (let [idx (->> anote
                 :metadata
                 :line
                 (state/ns->line->index namespace))]
    (state/update-in-state!
     [:ns->note-states
      namespace
      idx]
     transf)))

;; A note is realized by realizing all its pending values and rendering them.
(defn realize-note [anote note-state]
  (let [value    (evaluate-note anote)
        renderer (-> anote :kind state/kind->behaviour :value-renderer)
        _ (log/info {:renderer renderer
                     :dbg [(-> anote :kind state/kind->behaviour :value-renderer)
                           (-> anote :kind state/kind->behaviour)
                           (-> anote :kind)
                           (-> anote)
                           (state/kind->behaviour)]})
        rendered (-> value realize renderer)]
    (assoc note-state
           :value value
           :rendered rendered)))


(defn realize-note! [namespace anote]
  (update-note-state! namespace
                      (partial realize-note anote)
                      anote))

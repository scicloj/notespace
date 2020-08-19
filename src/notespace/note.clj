(ns notespace.note
  (:require [notespace.reader :as reader]
            [notespace.util :as u]
            [rewrite-clj.node]
            [notespace.source :as source]
            [notespace.context :as ctx]
            [notespace.events :as events]
            [notespace.view :as view]))

;; A note has a kind, possibly a label, a collection of forms, and the reader metadata.
(defrecord Note [kind label forms metadata])

;; A note's state has a return value, a rendered result, and a status.
(defrecord NoteState [value rendered status])

(defn note->note-state [namespace note]
  (->> note
       :metadata
       :line
       (ctx/sub-get-in :ns->line->index namespace)
       (ctx/sub-get-in :ns->note-state namespace)))

;; We can collect all toplevel forms in a namespace,
;; together with the reader metadata.
(defn ->ns-topforms-with-metadata [namespace]
  (->> namespace
       source/ns->source-filename
       reader/file->topforms-with-metadata
       (filter (comp :line meta))))

(defn ns-topform? [topform]
  (and (sequential? topform)
       (-> topform
           first
           (= 'ns))))

(defn strings-topform? [topform]
  (and (sequential? topform)
       (-> topform
           first
           string?)))

(defn kinds-set []
  (keys (ctx/sub-get-in :kind->behaviour)))

(defn metadata->kind [m]
  (->> (kinds-set)
       (filter m)
       first))

(defn topform-with-metadata->kind [tfwm]
  (or (-> tfwm meta metadata->kind)
      (if (strings-topform? tfwm)
        :md
        :naive)))

(defn topform-with-metadata->forms [tfwm]
  (if (-> tfwm meta :multi)
    tfwm
    [tfwm]))

;; Each toplevel form can be converted to a Note.
(defn topform-with-metadata->Note [tfwm]
  (let [m (meta tfwm)]
    (when-not (ns-topform? tfwm)
      (->Note (topform-with-metadata->kind tfwm)
              (:label m)
              (topform-with-metadata->forms tfwm)
              m))))

;; Thus we can collect all notes in a namespace.
(defn ns-notes [namespace]
  (->> namespace
       ->ns-topforms-with-metadata
       (map topform-with-metadata->Note)
       (filter some?)))

;; We can update our notes structures by reading the notes of a namespace.
;; We try not to update things that have not changed.

(defn evaluate-note [note]
  (try
    (->> note
         :forms
         (cons 'do)
         eval)
    (catch Exception e
      (throw (ex-info "Note evaluation failed."
                      {:note      note
                       :exception e}))) ))



(defn initial-note-state [note]
  (let [value (evaluate-note note)
        rendered (view/note->hiccup
                  note
                  value)]
    (->NoteState
     value
     rendered
     {:new true})))


;; TODO: Rethink
(defn different-note? [old-note new-note]
  (or (->> [old-note new-note]
           (map (comp :source :metadata))
           (apply not=))
      (->> [old-note new-note]
           (map (juxt :kind :forms))
           (apply not=))))

(defn merge-note [[old-note old-note-state]
                   new-note]
  (if (different-note? old-note new-note)
    [new-note (initial-note-state new-note)]
    [(merge old-note
            (select-keys new-note [:metadata]))
     old-note-state]))

(defn merge-notes [old-notes-and-states
                   new-notes]
  (mapv merge-note
        (concat old-notes-and-states (repeat nil))
        new-notes))

(defn reread-notes! [namespace]
  (let [old-notes            (ctx/sub-get-in :ns->notes namespace)
        old-notes-states     (ctx/sub-get-in :ns->note-states namespace)
        old-notes-and-states (map vector
                                  old-notes
                                  old-notes-states)
        needs-update         (or (not old-notes)
                                 (source/source-file-modified? namespace))
        notes-and-states     (if (not needs-update)
                               old-notes-and-states
                               (merge-notes old-notes-and-states
                                            (ns-notes namespace)))
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
                                (u/fmap (comp :idx u/only-one)))
            label->indices (->> notes
                                (map-indexed (fn [idx note]
                                               {:idx   idx
                                                :label (:label note)}))
                                (filter :label)
                                (group-by :label)
                                (u/fmap (partial mapv :idx)))]
        (ctx/handle {:event/type ::events/assoc-notes
                     :fx/sync    true
                     :namespace namespace
                     :notes (mapv first notes-and-states)
                     :note-states (mapv second notes-and-states)
                     :line->index line->index
                     :label->indices label->indices})))
     {:updated needs-update
      :n (count notes)
      :n-new (->> notes-and-states
                  (filter (fn [[_ note-state]]
                         (-> note-state
                             :status
                             :new)))
                  count)}))

;; We support various update transformations for notes' states.
(defn update-note-state! [namespace f note]
  (let [idx (->> note
                 :metadata
                 :line
                 (ctx/sub-get-in :ns->line->index namespace))]
    (ctx/handle {:event/type     ::events/assoc-notes
                 :fx/sync        true
                 :namespace      namespace
                 :idx idx
                 :f f})))

;; A note is realized by realizing all its pending values and rendering them.
(defn realize-note [note note-state]
  (let [value    (evaluate-note note)
        renderer (ctx/sub-get-in :kind->behaviour (:kind note) :value->hiccup)
        rendered (-> value u/realize renderer)]
    (assoc note-state
           :value value
           :rendered rendered)))

(defn realize-note! [namespace note]
  (update-note-state! namespace
                      (partial realize-note note)
                      note))

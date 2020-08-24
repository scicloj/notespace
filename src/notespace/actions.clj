(ns notespace.actions
  (:require [notespace.note :as note]
            [notespace.state :as state]
            [notespace.events :as events]
            [notespace.context :as ctx]
            [notespace.source :as source]
            [notespace.util :as u]))

;; We can update our notes structures by reading the notes of a namespace.
;; We try not to update things that have not changed.
(defn reread-notes! [namespace]
  (let [old-notes    (state/sub-get-in :ns->notes namespace)
        needs-update (or (not old-notes)
                         (source/source-file-modified? namespace))
        notes        (if (not needs-update)
                       old-notes
                       (note/merge-notes old-notes
                                         (note/ns-notes namespace)))]
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
        (ctx/handle {:event/type     ::events/assoc-notes
                     :fx/sync        true
                     :namespace      namespace
                     :notes          notes
                     :line->index    line->index
                     :label->indices label->indices})))
    {:updated needs-update
     :n       (count notes)
     :n-new   (->> notes
                   (filter (fn [note]
                             (-> note
                                 :status
                                 :stage
                                 (= :initial))))
                   count)}))

;; We support various update transformations for notes.
(defn update-note! [namespace f idx sync?]
  (ctx/handle {:event/type ::events/update-note
               :fx/sync    sync?
               :namespace  namespace
               :idx        idx
               :f          f}))

(defn eval-note! [namespace idx]
  (update-note! namespace
                (partial note/evaluated-note idx)
                idx
                true))

(defn eval-notes! [namespace]
  (dotimes [idx (-> namespace
                    reread-notes!
                    :n)]
    (eval-note! namespace idx)))

(defn realize-note! [namespace idx]
  (update-note! namespace
                note/realizing-note
                idx
                true)
  (update-note! namespace
                note/realized-note
                idx
                false))

(defn rerender-note! [namespace idx]
  (update-note! namespace
                note/note-with-updated-rendering
                idx
                false))

(defn eval-note-at-line! [namespace line]
  (reread-notes! namespace)
  (some->> line
           (state/sub-get-in :ns->line->index namespace)
           (eval-note! namespace)))

(defn realize-note-at-line! [namespace line]
  (reread-notes! namespace)
  (some->> line
           (state/sub-get-in :ns->line->index namespace)
           (realize-note! namespace)))

(defn eval-and-realize-note-at-line! [namespace line]
  (reread-notes! namespace)
  (when-let [idx (state/sub-get-in :ns->line->index namespace line)]
    (eval-note! namespace idx)
    (realize-note! namespace idx)))

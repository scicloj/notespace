(ns notespace.actions
  (:require [notespace.note :as note]
            [notespace.state :as state]
            [notespace.events :as events]
            [notespace.context :as ctx]
            [notespace.source :as source]
            [notespace.util :as u]))

;; We can update our notes structures by reading the notes of a namespace.
;; We try not to update things that have not changed.
(defn reread-notes! [anamespace]
  (let [old-notes    (state/sub-get-in :ns->notes anamespace)
        needs-update  (or (not old-notes)
                          (source/source-file-modified? anamespace))
        notes        (if (not needs-update)
                       old-notes
                       (note/merge-notes old-notes
                                         (note/ns-notes anamespace)))]
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
                     :namespace      anamespace
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
(defn update-note! [anamespace f idx sync?]
  (ctx/handle {:event/type ::events/update-note
               :fx/sync    sync?
               :namespace  anamespace
               :idx        idx
               :f          f}))

(defn act! [anamespace idx actions]
  (doseq [a actions]
    (a anamespace idx)))

(defn act-on-notes! [anamespace actions]
  (dotimes [idx (-> anamespace
                    reread-notes!
                    :n)]
    (act! anamespace idx actions)))

(defn act-on-notes-from-idx! [anamespace idx actions]
  (doseq [idx (->> anamespace
                   reread-notes!
                   :n
                   (range idx))]
    (act! anamespace idx actions)))

(defn line->idx [anamespace line]
  (state/sub-get-in :ns->line->index anamespace line))

(defn act-on-note-at-line! [anamespace line actions]
  (reread-notes! anamespace)
  (when-let [idx (line->idx anamespace line)]
    (doseq [a actions]
      (act! anamespace idx actions))))

(defn line->safe-idx [anamespace line]
  (or (state/sub-get-in :ns->line->index anamespace line)
      (some->> (state/sub-get-in :ns->notes anamespace)
               (map-indexed (fn [idx anote]
                              [idx (-> anote
                                       :metadata
                                       :line)]))
               (drop-while (fn [[idx line1]]
                             (< line1 line)))
               first
               first)))

(defn act-on-notes-from-line! [anamespace line actions]
  (let [{:keys [n]} (reread-notes! anamespace)]
    (when-let [initial-idx (line->safe-idx anamespace line)]
      (doseq [idx (range initial-idx n)]
        (act! anamespace idx actions)))))

(defn eval-note! [anamespace idx]
  (update-note! anamespace
                (partial note/evaluated-note anamespace idx)
                idx
                true))

(defn realize-note! [anamespace idx]
  (update-note! anamespace
                #'note/realizing-note
                idx
                true)
  (update-note! anamespace
                #'note/realized-note
                idx
                false))

(defn rerender-note! [anamespace idx]
  (update-note! anamespace
                #'note/realized-note
                idx
                false))

(defn assoc-input! [symbol value]
  (ctx/handle {:event/type ::events/assoc-input
               :fx/sync    true
               :symbol     symbol
               :value      value}))

(extend-protocol notespace.note/Acceptable
  nil
  (accept! [value anamespace idx])

  Object
  (accept! [value anamespace idx]
    (when (future? value)
      (future
        @value
        (rerender-note! anamespace idx))))

  clojure.lang.Atom
  (accept! [value anamespace idx]
    (add-watch
     value
     (str "k" (u/next-id :atom))
     (fn [_ _ _ _]
       (rerender-note! anamespace idx)))))

(defonce ns-lines
  (atom {}))

(defn first-line-of-change [anamespace]
  (let [old-lines (or (@ns-lines anamespace)
                      [])
        new-lines (with-open [rdr (clojure.java.io/reader
                                   (source/ns->source-filename
                                    anamespace))]
                    (vec (line-seq rdr)))
        num-added (- (count new-lines)
                     (count old-lines))]
    (swap! ns-lines assoc anamespace new-lines)
    (some->> (map (fn [i ol nl]
                    [i ol nl (= ol nl)])
                  (range)
                  old-lines
                  new-lines)
             (drop-while (fn [[i ol nl check]]
                           check))
             first
             first
             inc)))

(defn eval-and-realize-notes-from-change! [anamespace]
  (when-let [l (first-line-of-change
                anamespace)]
    (act-on-notes-from-line! anamespace l [eval-note!
                                           realize-note!])))

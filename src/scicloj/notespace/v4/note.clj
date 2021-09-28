(ns scicloj.notespace.v4.note)

(defonce current-id (atom 0))

(defn next-id []
  (swap! current-id inc))

(defn ->new-note [note-data]
  (assoc note-data ::id (next-id)))

(defn mark-status [note status]
  (-> note
      (assoc :status status)
      ->new-note))

(defn notes->counts [notes]
  (->> notes
       (mapcat (fn [note]
                 [(-> note
                      :status
                      :state)
                  (if (:comment? note)
                    :comment)]))
       (filter some?)
       frequencies))

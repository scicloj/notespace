(ns notespace.v4.note)

(defn mark-as-new [note]
  (assoc note :new? true))

(defn mark-status [note status]
  (assoc note :status status))

(defonce current-id (atom 0))

(defn next-id []
  (swap! current-id inc))

(defn ->new-note [note-data]
  (assoc note-data ::id (next-id)))

(defn start-line [note]
  (-> note :region first))

(defn shift [note line-offset]
  (-> note
      (update-in [:region 0] (partial + line-offset))
      (update-in [:region 2] (partial + line-offset))
      ->new-note))

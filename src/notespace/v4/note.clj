(ns notespace.v4.note)

(defn mark-as-new [note]
  (assoc note :new? true))

(defn mark-status [note status]
  (assoc note :status status))

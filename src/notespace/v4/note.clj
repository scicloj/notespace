(ns notespace.v4.note)

(defn mark-as-new [note]
  (assoc note :new? true))

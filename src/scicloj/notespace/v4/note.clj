(ns scicloj.notespace.v4.note
  (:require [scicloj.notespace.v4.kinds :as v4.kinds]))

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

(defn metadata->kind [m]
  (some->> m
           :tag
           resolve
           deref
           ((v4.kinds/kinds-set))))

(defn value->kind [value]
  (-> value
      meta
      :notespace/kind))

(defn value->behavior [value]
  (or (-> value
          v4.kinds/->behavior)
      (-> value
          value->kind
          (or v4.kinds/naive)
          v4.kinds/kind->behavior)))

(defn kind [note]
  (or (-> note
          :status
          :value
          value->kind)
      (-> note
          :meta
          metadata->kind)
      :notespace.kinds/naive))

(defn behavior [note]
  (-> note
      kind
      v4.kinds/kind->behavior))

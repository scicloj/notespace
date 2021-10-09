(ns scicloj.notespace.v4.note
  (:require [scicloj.kindly.api :as kindly]
            [scicloj.kindly.kind :as kind]))

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

(defn kind [note]
  (or (-> note
          :status
          :value
          kindly/value->kind)
      (-> note
          :meta
          kindly/metadata->kind)
      kind/naive))

(defn behaviour [note]
  (-> note
      kind
      kindly/kind->behaviour))

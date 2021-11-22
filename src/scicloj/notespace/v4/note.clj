(ns scicloj.notespace.v4.note
  (:require [scicloj.kindly.api :as kindly]
            [scicloj.kindly.kind :as kind]))

(defonce current-id (atom 0))

(defn next-id []
  (swap! current-id inc))

(defn ->new-note [note-data]
  (assoc note-data ::id (next-id)))

(defn merge-as-new-note [note details]
  (-> note
      (merge details)
      ->new-note))

(defn notes->counts [notes]
  (->> notes
       (mapcat (fn [note]
                 [(:status note)
                  (if (:comment? note)
                    :comment)]))
       (filter some?)
       frequencies))

(defn form->kind [form]
  (cond ;;
    (sequential? form)
    (let [f (first form)]
      (cond ;;
        ('#{ns
            def defonce defn defmacro
            defmethod defmulti
            deftype defrecord defprotocol
            extend-protocol extend-type
            require import comment}
         f)
        kind/void
        ;;
        (string? f)
        kind/md-nocode))
    ;;
    (string? form) ; TODO: make this work
    kind/md-nocode))

(defn kind [note]
  (-> note
      :meta
      kindly/metadata->kind
      (or (-> note :form form->kind))
      (or kind/naive)))

(defn behaviour [note]
  (or (-> note
          :value
          kindly/value->behaviour)
      (-> note
          kind
          kindly/kind->behaviour)))

(defn separator? [note]
  (and (:comment? note)
       (some? (re-matches #"^;;;;.*" (:source note)))))



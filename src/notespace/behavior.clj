(ns notespace.behavior
  (:require [notespace.state :as state]))

(defprotocol Behaving
  (->behavior [this]))

(extend-protocol Behaving
  Object
  (->behavior [this]
    nil))

(defn ->actual-behavior [kind value]
  (or (some-> value ->behavior)
      (let [actual-kind (-> value meta :notespace.kind (or kind))]
        ((state/sub-get-in :kind->behaviour) actual-kind))))


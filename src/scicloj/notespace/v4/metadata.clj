(ns scicloj.notespace.v4.metadata
  (:import [clojure.lang IObj]))

(deftype HasMeta [data metadata]
  ;;;;
  Object
  (equals [a b] (= (.data a) (.data b)))
  (hashCode [this] (.hashCode data))
  (toString [this] (.toString data))
  ;;;;
  IObj
  (meta [this]
    metadata)
  (withMeta [this new-meta]
    (HasMeta. data new-meta)))


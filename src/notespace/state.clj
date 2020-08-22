(ns notespace.state
  (:require [cljfx.api :as fx]
            [clojure.core.cache :as cache]))

(def the-context
  (atom
   (fx/create-context {} cache/lru-cache-factory)))

;; A conveniene function for subscribing to the value
;; at a given path inside the context.
(defn sub-get-in [& path]
  (fx/sub-val @the-context get-in path))

(-> @the-context
    (fx/sub-val :ns->line->index)
    vals
    first
    (->> (sort-by first)))

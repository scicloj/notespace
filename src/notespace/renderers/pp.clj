(ns notespace.renderers.pp
  (:require [clojure.pprint :as pp]
            [cljfx.api :as fx]))

(defn renderer [context]
  (some-> context
          (fx/sub :notes)
          pp/pprint))

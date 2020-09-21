(ns notespace.renderers.pprint
  (:require [clojure.pprint :as pp]
            [cljfx.api :as fx]))

(defn renderer [_ new-context]
  (some-> new-context
          (fx/sub :notes)
          pp/pprint))

(ns scicloj.notespace.v4.api
  (:require [scicloj.notespace.v4.state :as v4.state]
            [scicloj.notespace.v4.system :as v4.system]))

(defn restart! [config]
  (v4.state/init!)
  (v4.system/restart! config))



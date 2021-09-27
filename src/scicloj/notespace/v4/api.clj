(ns scicloj.notespace.v4.api
  (:require [scicloj.notespace.v4.system :as v4.system]))

(defn restart! [config]
  (v4.system/restart! config))



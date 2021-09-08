(ns notespace.v4.loop
  (:require [notespace.v4.events.handle :as v4.handle]
            [notespace.v4.events.channels :as v4.channels]))

(defonce started
  (v4.channels/start! v4.handle/handle))

(defn push-event [event]
  (v4.channels/push-event event))

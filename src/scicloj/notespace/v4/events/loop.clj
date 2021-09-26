(ns scicloj.notespace.v4.events.loop
  (:require [scicloj.notespace.v4.events.handle :as v4.handle]
            [scicloj.notespace.v4.events.channels :as v4.channels]
            [scicloj.notespace.v4.state :as v4.state]))

(defn handle-and-transact [event]
  (try
    (-> event
        (assoc :state @v4.state/*state)
        v4.handle/handle
        v4.state/reset-state!)
    (catch Exception e (println e))))

(defonce started
  (v4.channels/start! #'handle-and-transact))

(defn push-event [event]
  (v4.channels/push-event event))

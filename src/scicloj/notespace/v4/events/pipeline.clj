(ns scicloj.notespace.v4.events.pipeline
  (:require [scicloj.notespace.v4.events.handle :as v4.handle]
            [scicloj.notespace.v4.events.channels :as v4.channels]
            [scicloj.notespace.v4.state :as v4.state]))

(defn handle-and-transact [events]
  (try
    (doseq [event events]
      (-> event
          (assoc :state @v4.state/*state)
          v4.handle/handle
          (v4.state/reset-state! false)))
    (catch Exception e (println e)))
  (when (seq events)
    (v4.state/reset-frontend!)))

(defn start! []
  (let [pipeline (v4.channels/start! #'handle-and-transact)]
    (v4.state/reset-pipeline! pipeline)
    pipeline))

(defn stop! [pipeline]
  (v4.state/reset-pipeline! nil)
  (some-> pipeline
          :stop
          (#(%))))

(defn process-event [event]
  (some-> (v4.state/pipeline)
          :process
          (#(% event))))


(ns scicloj.notespace.v4.systems.events
  (:require [integrant.core :as integrant]
            [scicloj.notespace.v4.watch :as v4.watch]
            [scicloj.notespace.v4.events.pipeline :as v4.pipeline]))

(def default-config
  {:watcher/beholder nil
   :pipeline/pipeline nil})

(defmethod integrant/init-key :watcher/beholder [_ _]
  (v4.watch/watch))

(defmethod integrant/halt-key! :adapter/jetty [_ watcher]
  (v4.watch/stop watcher))

(defmethod integrant/init-key :pipeline/pipeline [_ _]
  (v4.pipeline/start!))

(defmethod integrant/halt-key! :adapter/jetty [_ pipeline]
  (v4.pipeline/stop! pipeline))

(defonce *system
  (atom nil))

(defn restart! []
  (some-> @*system
          integrant/halt!)
  (->> default-config
       integrant/init
       (reset! *system)))

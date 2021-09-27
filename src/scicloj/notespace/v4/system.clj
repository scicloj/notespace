(ns scicloj.notespace.v4.system
  (:require [integrant.core :as integrant]
            [scicloj.notespace.v4.frontend.engine :as v4.frontend.engine]
            [scicloj.notespace.v4.watch :as v4.watch]
            [scicloj.notespace.v4.events.pipeline :as v4.pipeline]))

(def default-config
  {:server/gorilla-notes {:port 1903}
   :watcher/beholder nil
   :pipeline/pipeline nil})

(defmethod integrant/init-key :server/gorilla-notes [_ options]
  (v4.frontend.engine/start! options))

(defmethod integrant/halt-key! :server/gorilla-notes [_ server]
  (v4.frontend.engine/stop! server))

(defmethod integrant/init-key :watcher/beholder [_ _]
  (v4.watch/watch))

(defmethod integrant/halt-key! :adapter/jetty [_ watcher]
  (v4.watch/stop watcher))

(defmethod integrant/init-key :pipeline/pipeline [_ _]
  (v4.pipeline/start!))

(defmethod integrant/halt-key! :adapter/jetty [_ pipeline]
  (v4.pipeline/stop! pipeline))

(defn init
  ([]
   (init default-config))
  ([config]
   (->> config
        (merge default-config)
        integrant/init)))

(defonce *system
  (atom nil))

(defn restart! [{:keys [port open-browser?]}]
  (some-> @*system
          integrant/halt!)
  (->> (when port {:server/gorilla-notes {:port port}})
       (merge {})
       init
       (reset! *system))
  (when open-browser?
    (v4.frontend.engine/browse!)))

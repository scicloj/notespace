(ns scicloj.notespace.v4.systems.frontend
  (:require [integrant.core :as integrant]
            [scicloj.notespace.v4.frontend.engine :as v4.frontend.engine]))

(def default-config
  {:server/frontend {:port 1903}})

(defmethod integrant/init-key :server/frontend [_ options]
  (v4.frontend.engine/start! options))

(defmethod integrant/halt-key! :server/frontend [_ server]
  (v4.frontend.engine/stop! server))

(defonce *system
  (atom nil))

(defn init
  ([]
   (init default-config))
  ([config]
   (->> config
        (merge default-config)
        integrant/init)))

(defn restart! [{:keys [port open-browser?]}]
  (some-> @*system
          integrant/halt!)
  (->> (when port {:server/frontend {:port port}})
       (merge {})
       init
       (reset! *system))
  (when open-browser?
    (v4.frontend.engine/browse!)))

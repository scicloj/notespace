(ns scicloj.notespace.v4.system
  (:require [integrant.core :as integrant]
            [scicloj.notespace.v4.frontend.engine :as v4.frontend.engine]
            [scicloj.notespace.v4.watch :as v4.watch]))

(def default-config
  {:server/gorilla-notes {:port 1903}
   :watcher/beholder nil})

(defmethod integrant/init-key :server/gorilla-notes [_ options]
  (v4.frontend.engine/start! options))

(defmethod integrant/halt-key! :server/gorilla-notes [_ server]
  (v4.frontend.engine/stop! server))

(defmethod integrant/init-key :watcher/beholder [_ _]
  (v4.watch/watch))

(defmethod integrant/halt-key! :adapter/jetty [_ watcher]
  (v4.watch/stop watcher))

(defn init
  ([]
   (init default-config))
  ([config]
   (->> config
        (merge default-config)
        integrant/init)))

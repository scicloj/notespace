(ns notespace.v4.system
  (:require [integrant.core :as integrant]
            [gorilla-notes.core :as gorilla-notes]
            [notespace.v4.watch :as v4.watch]))

(def config
  {:server/gorilla-notes {:port 1903}
   :watcher/beholder nil})

(defmethod integrant/init-key :server/gorilla-notes [_ options]
  (gorilla-notes/start-server! options)
  (gorilla-notes/reset-notes!))

(defmethod integrant/halt-key! :server/gorilla-notes [_ server]
  (server))

(defmethod integrant/init-key :watcher/beholder [_ _]
  (v4.watch/watch))

(defmethod integrant/halt-key! :adapter/jetty [_ watcher]
  (v4.watch/stop watcher))

(defn init []
  (integrant/init config))

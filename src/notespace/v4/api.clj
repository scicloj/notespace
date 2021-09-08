(ns notespace.v4.api
  (:require [notespace.v4.state :as v4.state]
            [gorilla-notes.core :as gn]))

(defn start [{:keys [port open-browser?]}]
  (v4.state/start-gorilla-notes-server {:port port})
  (v4.state/start)
  (when open-browser?
    (gn/browse-http-url)))

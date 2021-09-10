(ns notespace.v4.api
  (:require [notespace.v4.state :as v4.state]
            [notespace.v4.watch :as v4.watch]
            [notespace.v4.system :as v4.system]
            [gorilla-notes.core :as gn]))

(defn start [{:keys [port open-browser?]}]
  (v4.state/start)
  (v4.system/init)
  (when open-browser?
    (gn/browse-http-url)))

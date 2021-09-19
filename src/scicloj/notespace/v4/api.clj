(ns scicloj.notespace.v4.api
  (:require [scicloj.notespace.v4.state :as v4.state]
            [scicloj.notespace.v4.watch :as v4.watch]
            [scicloj.notespace.v4.system :as v4.system]
            [gorilla-notes.core :as gn]))

(defn start [{:keys [port open-browser?]}]
  (v4.state/start!)
  (v4.system/init)
  (when open-browser?
    (gn/browse-http-url)))




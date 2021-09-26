(ns scicloj.notespace.v4.api
  (:require [scicloj.notespace.v4.state :as v4.state]
            [scicloj.notespace.v4.watch :as v4.watch]
            [scicloj.notespace.v4.system :as v4.system]))

(defn start [{:keys [port open-browser?]}]
  (v4.state/start!)
  (v4.system/init
   (merge {}
          (when port {:server/gorilla-notes {:port port}})))
  (when open-browser?
    (gn/browse-http-url)))




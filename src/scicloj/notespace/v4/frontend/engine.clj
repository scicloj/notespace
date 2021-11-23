(ns scicloj.notespace.v4.frontend.engine
  (:require [scicloj.notespace.v4.frontend.protocol :as prot]
            [scicloj.notespace.v4.frontend.gorilla-notes :as v4.frontend.gorilla-notes]
            #_[scicloj.notespace.v4.frontend.clerk :as v4.frontend.clerk]))

(def frontend
  (v4.frontend.gorilla-notes/->GNFrontend)
  #_(v4.frontend.clerk/->ClerkFrontend))

(defn start! [options]
  (prot/start! frontend options))

(defn stop! [server]
  (prot/stop! frontend server))

(defn reset-header! [header]
  (prot/reset-header! frontend header))

(defn sync-widgets! [mode broadcast? id-fn widget-fn widgets-data]
  (prot/sync-widgets! frontend mode broadcast? id-fn widget-fn widgets-data))

(defn restrict-modes! [modes]
  (prot/restrict-modes! frontend modes))

(defn broadcast-widgets! []
  (prot/broadcast-widgets! frontend))

(defn browse! []
  (prot/browse! frontend))

(defn render-as-html! [html-path]
  (prot/render-as-html! frontend html-path))

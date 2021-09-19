(ns scicloj.notespace.v4.frontend.engine
  (:require [gorilla-notes.core :as gorilla-notes]))

(defn start! [options]
  (let [server (gorilla-notes/start-server! options)]
    (gorilla-notes/reset-notes!)
    (gorilla-notes/merge-new-options!
     (merge
      {:notes-in-cards? false
       :reverse-notes?  false}))
    server))

(defn stop! [server]
  (server))

(defn reset-header! [header]
  (gorilla-notes/merge-new-options!
   {:custom-header header}))

(defn reset-widgets! []
  (gorilla-notes/reset-notes!))

(defn add-widget! [hiccup]
  (gorilla-notes/add-note! hiccup))

(defn assoc-widget! [idx hiccup]
  (gorilla-notes/assoc-note! idx hiccup))

(defn remove-widget! [idx]
  (gorilla-notes/remove-note! idx))

(defn sync-widgets! [id-fn widget-fn widgets-data]
  (gorilla-notes/reset-notes-with-content!
   {:ids-and-content (->> widgets-data
                          (map (juxt id-fn widget-fn)))}))

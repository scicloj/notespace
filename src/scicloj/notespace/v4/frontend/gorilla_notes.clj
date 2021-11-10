(ns scicloj.notespace.v4.frontend.gorilla-notes
  (:require [scicloj.notespace.v4.frontend.protocol :as prot]
            [gorilla-notes.core :as gorilla-notes]))

(deftype GNFrontend []

  prot/Frontend

  (start! [this options]
    (let [server (gorilla-notes/start-server! options)]
      (gorilla-notes/reset-notes!)
      (gorilla-notes/merge-new-options!
       (merge
        {:notes-in-cards? false
         :reverse-notes?  false}))
      server))

  (stop! [this server]
    (server))

  (reset-header! [this header]
    (gorilla-notes/merge-new-options!
     {:custom-header header}))

  (reset-widgets! [this ]
    (gorilla-notes/reset-notes!))

  (add-widget! [this hiccup]
    (gorilla-notes/add-note! hiccup))

  (assoc-widget! [this idx hiccup]
    (gorilla-notes/assoc-note! idx hiccup))

  (remove-widget! [this idx]
    (gorilla-notes/remove-note! idx))

  (sync-widgets! [this id-fn widget-fn widgets-data]
    (gorilla-notes/reset-notes-with-content!
     {:ids-and-content (->> widgets-data
                            (map (juxt id-fn widget-fn)))}))

  (browse! [this ]
    (gorilla-notes/browse-http-url))

  (render-as-html! [this html-path]
    (gorilla-notes/render-current-state! html-path)))

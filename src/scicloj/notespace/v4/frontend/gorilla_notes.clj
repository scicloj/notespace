(ns scicloj.notespace.v4.frontend.gorilla-notes
  (:require [scicloj.notespace.v4.frontend.protocol :as prot]
            [gorilla-notes.core :as gorilla-notes]))


(deftype GNFrontend []

  prot/Frontend

  (start! [this options]
    (let [server (gorilla-notes/start-server! options)]
      (gorilla-notes/reset-notes! :notespace)
      (gorilla-notes/reset-notes! :last-eval)
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

  (sync-widgets! [this mode broadcast? id-fn widget-fn widgets-data]
    ;; (when (= mode :last-eval)
    ;;   (println {:ids-and-content (->> widgets-data
    ;;                                   (map (juxt id-fn widget-fn)))}))
    (gorilla-notes/reset-notes-with-content!
     mode
     {:ids-and-content (->> widgets-data
                            (map (juxt id-fn widget-fn)))
      :broadcast? broadcast?}))

  (broadcast-widgets! [this]
    (gorilla-notes/broadcast-content-ids!))

  (browse! [this]
    (gorilla-notes/browse-http-url))

  (render-as-html! [this html-path]
    (gorilla-notes/render-current-state! [:notespace] html-path)))



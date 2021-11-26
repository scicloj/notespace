(ns scicloj.notespace.v4.frontend.gorilla-notes
  (:require [scicloj.notespace.v4.frontend.protocol :as prot]
            [gorilla-notes.core :as gorilla-notes]
            [gorilla-notes.styles.bootswatch :as bootswatch]
            [gorilla-notes.styles.highlight-js :as highlight-js]))


(deftype GNFrontend []

  prot/Frontend

  (start! [this options]
    (let [server (gorilla-notes/start-server! options)]
      (gorilla-notes/reset-notes! :notespace)
      (gorilla-notes/reset-notes! :last-eval)
      (gorilla-notes/merge-new-options!
       {:notes-in-cards? false
        :reverse-notes?  false
        :main-div-class  nil
        :page            {:bootswatch-style   bootswatch/sandstone
                          :highlight-js-theme highlight-js/gradient-light}})
      server))

  (stop! [this server]
    (server))

  (reset-header! [this header]
    (gorilla-notes/merge-new-options!
     {:custom-header header}))

  (sync-widgets! [this mode broadcast? id-fn widget-fn widgets-data]
    (gorilla-notes/reset-notes-with-content!
     mode
     {:ids-and-content (->> widgets-data
                            (map (juxt id-fn widget-fn)))
      :broadcast?      broadcast?}))

  (restrict-modes! [this modes]
    (gorilla-notes/restrict-modes!
     modes
     :broadcast? false))

  (broadcast-widgets! [this]
    (gorilla-notes/broadcast-content-ids!))

  (browse! [this]
    (gorilla-notes/browse-http-url))

  (render-as-html! [this html-path]
    (gorilla-notes/render-current-state! [:notespace] html-path)))



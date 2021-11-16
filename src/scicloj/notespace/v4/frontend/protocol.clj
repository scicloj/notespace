(ns scicloj.notespace.v4.frontend.protocol)

(defprotocol Frontend
  (start! [this options])
  (stop! [this server])
  (reset-header! [this header])
  (sync-widgets! [this mode broadcast? id-fn widget-fn widgets-data])
  (broadcast-widgets! [this])
  (browse! [this])
  (render-as-html! [this html-path]))


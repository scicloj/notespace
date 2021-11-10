(ns scicloj.notespace.v4.frontend.protocol)

(defprotocol Frontend
  (start! [this options])
  (stop! [this server])
  (reset-header! [this header])
  (reset-widgets! [this])
  (add-widget! [this hiccup])
  (assoc-widget! [this idx hiccup])
  (remove-widget! [this idx])
  (sync-widgets! [this id-fn widget-fn widgets-data])
  (browse! [this])
  (render-as-html! [this html-path]))


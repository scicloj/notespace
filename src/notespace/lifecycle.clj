(ns notespace.lifecycle
  (:require [notespace.context :as ctx]
            [notespace.events :as events]
            [notespace.defaults :as defaults]
            [notespace.renderers.gorilla-notes :as gn]))

(defn init [& {:keys [open-browser? port]
               :or   {open-browser? false}}]
  (ctx/handle
   {:event/type    ::events/reset-but-keep-config
    :fx/sync       true
    :initial-state defaults/initial-state})
  (println [:port port])
  (if port
    (gn/init :port port)
    (gn/init))
  (when open-browser?
    (gn/browse))
  (ctx/unmount-renderer #'gn/renderer)
  (ctx/mount-renderer #'gn/renderer)
  :ok)

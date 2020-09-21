(ns notespace.lifecycle
  (:require [notespace.context :as ctx]
            [notespace.events :as events]
            [notespace.defaults :as defaults]
            [notespace.renderers.gorilla-notes :as gn]))

(defn init [& {:keys [open-browser?]
               :or   {open-browser? false}}]
  (ctx/handle
   {:event/type    ::events/reset
    :fx/sync       true
    :initial-state defaults/initial-state})
  (gn/init)
  (when open-browser?
    (gn/browse))
  (ctx/unmount-renderer #'gn/renderer)
  (ctx/mount-renderer #'gn/renderer)
  :ok)

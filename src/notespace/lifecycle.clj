(ns notespace.lifecycle
  (:require [notespace.context :as ctx]
            [notespace.events :as events]
            [notespace.defaults :as defaults]
            [notespace.renderers.pp :as pp-renderer]))

(defn init []
  (ctx/handle
   {:event/type    ::events/reset
    :fx/sync       true
    :initial-state defaults/initial-state})
  (ctx/mount-renderer pp-renderer/renderer))

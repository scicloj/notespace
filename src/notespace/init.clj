(ns notespace.init
  (:require [notespace.context :as ctx]
            [notespace.defaults :as defaults]
            [notespace.events :as events]
            [clojure.pprint :as pp]
            [notespace.renderers.pp :as pp-renderer]))

(defn init []
  (ctx/handle
   {:event/type ::events/reset
    :fx/sync    true
    :initial-state defaults/initial-state})
  (ctx/mount-renderer pp-renderer/renderer))

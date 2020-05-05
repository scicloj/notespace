(ns notespace.v2.init
  (:require [notespace.v2.config :as config]
            [notespace.v2.state :as state]
            [notespace.v2.kinds :as kinds]))

(defn init! []
  (state/reset-state!)
  (config/set-default-config!)
  (kinds/define-base-kinds!))


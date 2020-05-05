(ns notespace.v2.config
  (:import java.io.File)
  (:require [cambium.core :as log]
            [notespace.v2.io :as io]
            [notespace.v2.state :as state]))

(defn set-default-target-path! [target-path]
  (state/assoc-in-state! [:config :target-path] target-path)
  (io/make-path target-path)
  (log/info [::set-default-target-path! target-path]))

(defn set-default-config! []
  (let [config  {:live-reload-port 5678
                 :css              :basic
                 :target-path      (io/make-path "doc")}]
    (state/assoc-in-state! [:config] config)
    (log/info [::set-default-config! config])))


(ns notespace.v2.config
  (:import java.io.File)
  (:require [cambium.core :as log]))

;; Here we hold general defaults.

(def defaults
  (atom {:live-reload-port 5678}))

;; Here we can hold namespace-specific configuration.

(def ns->config (atom {}))

(defn config-this-ns! [conf]
  (swap! ns->config assoc *ns* conf))


(defn set-default-target-path! [target-path]
  (swap! defaults assoc :target-path target-path)
  (.mkdirs ^File (File. target-path))
  (log/info [::set-default-target-path target-path]))

(set-default-target-path! "doc")

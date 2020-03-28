(ns notespace.v2.config)

;; Here we hold general defaults.

(def defaults
  (atom {:live-reload-port 5678
         :target-path "doc"}))

;; Here we can hold namespace-specific configuration.

(def ns->config (atom {}))

(defn config-this-ns! [conf]
  (swap! ns->config assoc *ns* conf))

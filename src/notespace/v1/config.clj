(ns notespace.v1.config)

;; Here we can hold namespace-specific configuration.

(def ns->config (atom {}))

(defn config-this-ns! [conf]
  (swap! ns->config assoc *ns* conf))


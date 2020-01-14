(ns notespace.v1.source
  (:require [clojure.string :as string]
            [notespace.v1.config :refer [ns->config]]
            [clojure.java.io :as io]))

;; We keep track of changes in source files corresponding to namespaces.
(def ns->last-modification (atom {}))

;; For a given namespace, we can find the location of the corresponding source file.
(defn src-or-test [namespace]
  (if (-> namespace
          str
          (string/split #"-")
          last
          (= "test"))
    "test"
    "src"))

(defn ns->source-filename [namespace]
  (let [base-path (-> namespace
                      (@ns->config)
                      :base-path
                      (or (str (src-or-test namespace)
                               "/")))]
    (str base-path
         (-> namespace
             str
             (string/replace "." "/")
             (string/replace "-" "_"))
         ".clj")))

(defn source-file-modified? [namespace]
  (let [previous-modifiction-time (@ns->last-modification namespace)
        modification-time (-> namespace ns->source-filename io/file (.lastModified))]
    (swap! ns->last-modification assoc namespace modification-time)
    (not= previous-modifiction-time modification-time)))

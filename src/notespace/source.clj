(ns notespace.source
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            [notespace.context :as ctx]
            [notespace.events :as events]
            [notespace.state :as state]))

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
  (let [base-path (or (state/sub-get-in :ns->config namespace :base-path)
                      (str (src-or-test namespace)
                           "/"))]
    (str base-path
         (-> namespace
             str
             (string/replace "." "/")
             (string/replace "-" "_"))
         ".clj")))

(defn source-file-modified? [namespace]
  (let [previous-modifiction-time (state/sub-get-in :ns->last-modification namespace)
        modification-time (-> namespace ns->source-filename io/file (.lastModified))]
    (ctx/handle
     {:event/type ::events/file-modified
      :fx/sync true
      :namespace namespace
      :modification-time modification-time})
    (not= previous-modifiction-time modification-time)))

(ns notespace.v2.source
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            [notespace.v2.state :as state]))

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
                      state/ns->config
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
  (let [previous-modifiction-time (state/get-in-state
                                   [:ns->last-modification namespace])
        modification-time (-> namespace ns->source-filename io/file (.lastModified))]
    (state/assoc-in-state!
     [:ns->last-modification namespace]
     modification-time)
    (not= previous-modifiction-time modification-time)))

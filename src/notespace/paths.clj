(ns notespace.v2.paths
  (:require [notespace.v2.state :as state]
            [clojure.string :as string]
            [notespace.v2.io :as io])
  (:import java.io.File))

(defn ns->target-path [namespace]
  (let [dirname (str (state/config [:target-path])
                     "/"
                     (-> namespace str (string/replace "." "/"))
                     "/")
        dir     (File. dirname)]
    (when-not (.exists dir)
      (.mkdirs dir))
    dirname))


;; Any namespace has a corresponding output html file.
(defn ns->out-filename [namespace]
  (format "%s/index.html" (ns->target-path namespace)))

(defn copy-to-ns-target-path [source-uri target-filename]
  (io/copy source-uri
           (str (ns->target-path *ns*)
                "/"
                target-filename)))

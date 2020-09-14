(ns notespace.paths
  (:require [notespace.state :as state]
            [clojure.string :as string])
  (:import java.io.File))

(defn ns->target-path [namespace]
  (let [dirname (str (state/sub-get-in :config :target-base-path)
                     "/"
                     (-> namespace str (string/replace "." "/"))
                     "/")
        dir     (File. dirname)]
    (when-not (.exists dir)
      (.mkdirs dir))
    (format "%s/index.html" dirname)))


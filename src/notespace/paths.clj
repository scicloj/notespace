(ns notespace.paths
  (:require [notespace.state :as state]
            [clojure.string :as string])
  (:import java.io.File
           org.apache.commons.io.FileUtils))

(defn ns-target-base-path
  ([] (ns-target-base-path *ns*))
  ([namespace]
   (let [target-base-path (state/sub-get-in :config :target-base-path)
         _                (assert target-base-path
                                  "The target-base-path should be non-nil. Have you initialized Notespace?")
         dirname          (str target-base-path
                               "/"
                               (-> namespace str (string/replace "." "/"))
                               "/")
         dir              (File. dirname)]
     (when-not (.exists dir)
       (.mkdirs dir))
     dir)))

(defn ns->target-path
  ([]
   (ns->target-path *ns*))
  ([namespace]
   (format "%s/index.html"
           (ns-target-base-path namespace))))

(def files-dirname "notespace-files")

(def dynamic-files-dirname (str "resources/public/" files-dirname))

(defn clean-dynamic-files-dir []
  (let [dir (File. dynamic-files-dirname)]
    (when-not (.exists dir)
      (.mkdirs dir))
    (FileUtils/cleanDirectory dir)))

(defn file-path-for-url [filename]
  (format "%s/%s"
          files-dirname
          filename))

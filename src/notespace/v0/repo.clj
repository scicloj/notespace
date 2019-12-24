(ns notespace.v0.repo
  "Figuring out details related to the github repo holding a given notespace."
  (:require [clojure.java.io :as io]
            [clojure.java.shell :refer [sh]]
            [clojure.string :as string])
  (:import (java.io File)))


(defn working-directory []
  (io/file (System/getProperty "user.dir")))

(defn path-relative-to-git-home []
  (loop [relative-path ""
         base-dir      (working-directory)]
    (if (some (fn [^File f]
                (-> f (.getName) (= ".git")))
              (file-seq base-dir))
      relative-path
      (when-let [parent (.getParentFile base-dir)]
        (recur (str (.getName base-dir) "/" relative-path)
               parent)))))

(defn origin-url []
  (-> (sh "git" "remote" "get-url" "origin")
      :out))

(defn repo-url []
  (some-> (origin-url)
          seq
          (->> (apply str))
          (string/replace #"\n" "")
          (string/replace #"git@github.com:|.git" "")
          (->> (str "https://github.com/"))))


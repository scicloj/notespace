(ns notespace.v1.repo
  "Figuring out details related to the github repo holding a given notespace."
  (:require [clojure.java.io :as io]
            [clojure.java.shell :refer [sh]]
            [clojure.string :as string]
            [notespace.v1.source :as source])
  (:import (java.io File)
           (java.nio.file Files)))

(defn working-directory []
  (io/file (System/getProperty "user.dir")))

(defn path-relative-to-git-home []
  (loop [relative-path ""
         base-dir      (working-directory)]
    (if (some (fn [^File f]
                (-> f (.getName) (= ".git")))
              (.listFiles ^File (io/file base-dir)))
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

(defn ns-url [namespace]
  (some-> (repo-url)
          (str
           "/tree/master/"
           (path-relative-to-git-home)
           (source/ns->source-filename namespace))))

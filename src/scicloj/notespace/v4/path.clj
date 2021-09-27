(ns scicloj.notespace.v4.path
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [scicloj.notespace.v4.log :as v4.log])
  (:import (java.nio.file LinkOption)))

(set! *warn-on-reflection* true)

(defn clj-path? [path]
  (re-matches #".*\.clj$" path))

(defn real-path [path]
  (let [file (io/file path)]
    (when (.exists file)
      (-> path
          io/file
          (.toPath)
          (.toRealPath (into-array LinkOption []))
          str))))

(defn current-directory-real-path []
  (-> "user.dir"
      (System/getProperty)
      real-path))

(defn path-relative-to-current-directory [path]
  (string/replace (real-path path)
                  (str (current-directory-real-path) "/")
                  ""))


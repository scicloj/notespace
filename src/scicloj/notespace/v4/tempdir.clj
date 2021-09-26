(ns notespace.v4.tempdir
  (:require [babashka.fs :as fs]
            [clojure.java.io :as io])
  (:import [java.io File]
           [java.nio.file Files Path Paths]
           [java.nio.file.attribute FileAttribute]))

(def *current-tempdir (atom  nil))

(def root "notespace-tmp/")

(defn make-new! []
  (when-not (fs/exists? root)
    (fs/create-dirs root))
  (->> (Files/createTempDirectory
        (Paths/get root (into-array String []))
        "tmp-"
        (into-array FileAttribute []))
       (reset! *current-tempdir)))

(defn get-current! []
  (-> @*current-tempdir
      (or (make-new!))
      str))

(defn get-tmp-filepath! [extension]
  (.getPath
   ^File
   (File/createTempFile "tmp"
                       extension
                       (io/file (get-current!)))))

(defn delete-all! []
  (-> root
      io/file
      fs/delete-tree))

(defn cleanup! []
  (delete-all!)
  (reset! *current-tempdir nil))

;; Seems to not work.
(defonce ^:private shutdown-hook-registered
  (do (.addShutdownHook (Runtime/getRuntime)
                        (Thread. cleanup!))
      true))

(comment
  (get-current!)
  (get-tmp-filepath! ".csv")
  (cleanup!))

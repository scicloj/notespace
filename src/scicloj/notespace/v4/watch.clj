(ns scicloj.notespace.v4.watch
  (:require [clojure.pprint :as pp]
            [nextjournal.beholder :as beholder]
            [scicloj.notespace.v4.events.pipeline :as v4.pipeline])
  (:import sun.nio.fs.UnixPath))

(defn handle [{:keys [^UnixPath path]}]
  (let [path-str (.toString path)]
    (when (re-matches #".*\.clj$" path-str)
      (v4.pipeline/process-event
       {:path       path-str
        :event/type :scicloj.notespace.v4.events.handle/buffer-update}))))

(defn watch []
  (beholder/watch #'handle "."))

(defn stop [watcher]
  (beholder/stop watcher))



(ns notespace.v4.watch
  (:require [clojure.pprint :as pp]
            [nextjournal.beholder :as beholder]
            [notespace.v4.loop :as v4.loop])
  (:import sun.nio.fs.UnixPath))

(defn handle [{:keys [^UnixPath path]}]
  (let [path-str (.toString path)]
    (when (re-matches #".*\.clj$" path-str)
      (v4.loop/push-event
       {:path       path-str
        :event-type :buffer-update}))))

(defn watcher []
  (beholder/watch #'handle "."))


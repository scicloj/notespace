(ns notespace.v4.log
  (:require [clojure.pprint :as pp]
            [gorilla-notes.core :as gn]))

(def log-path "/tmp/notespace-log.edn")

(defn empty-logfile []
  ;; Run in a different thread.
  ;; This is important, since if you call this function from your REPL,
  ;; it may cause the logging of some events as a side effect.
  ;; We wish not to block that, but just wait a bit for it to happen,
  ;; and then empty the file.
  (future
    ;; Wait a bit.
    (Thread/sleep 3000)
    ;; Now we can empty it.
    (spit log-path "")))

(defn log [msg]
  (spit
   log-path
   (str msg "\n")
   :append true))

(defn log-data
  ([data]
   (log-data nil data))
  ([title data]
   (->> data
        (#(if title
            [title %]
            %))
        pp/pprint
        with-out-str
        log)
   data))


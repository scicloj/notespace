(ns scicloj.notespace.v4.messages
  (:require [clojure.pprint :as pp]
            [scicloj.notespace.v4.path :as v4.path]))

(defn time-string []
  (.format (new java.text.SimpleDateFormat "HH:mm:ss")
           (java.util.Date.)))

(defn add-message [messages message]
  (-> messages
      (conj message)
      (->> (take-last 20))
      vec))

(defn add-formatted-message [messages label info]
  (add-message
   messages
   [:div
    [:p/code (-> {label (time-string)}
                 (merge info)
                 (#(if (:path %)
                     (update % :path v4.path/path-relative-to-current-directory)
                     %))
                 pp/pprint
                 with-out-str)]]))


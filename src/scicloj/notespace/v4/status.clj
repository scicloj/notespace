(ns scicloj.notespace.v4.status
  (:require [clojure.pprint :as pp]
            [scicloj.notespace.v4.path :as v4.path]
            [scicloj.notespace.v4.state :as v4.state]))

(defn time-string []
  (.format (new java.text.SimpleDateFormat "HH:mm:ss")
           (java.util.Date.)))

(defn add [label info]
  (v4.state/add-status
   [:div
    [:p/code (-> {label (time-string)}
                 (merge info)
                 (#(if (:path %)
                    (update % :path v4.path/path-relative-to-current-directory)
                    %))
                 pp/pprint
                 with-out-str)]]))

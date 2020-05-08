(ns notespace.v2.init
  (:require [notespace.v2.config :as config]
            [notespace.v2.state :as state]
            [notespace.v2.kinds :as kinds]
            [clojure.core.async :as async]
            [cambium.core :as log]))

(def current-loop-id (atom nil))

(defn run-state-change-loop! []
  (reset! current-loop-id (rand-int 999999))
  (let [this-loop-id @current-loop-id]
    (async/go-loop []
      (let [state-change (async/<! state/state-changes)]
        (doseq [l (state/get-in-state [:state-listeners])]
          (l state-change)))
      (if (= this-loop-id @current-loop-id)
        (recur)))))

(defn logging-state-listener [[change-type paths-and-_]]
  (->> paths-and-_
       (partition 2)
       (mapv first)
       (vector change-type)
       log/debug))

(defn init! []
  (require 'notespace.v2.note)
  (state/reset-state!)
  (config/set-default-config!)
  (kinds/define-base-kinds!)
  (state/assoc-in-state!
   [:state-listeners] [#'logging-state-listener])
  (run-state-change-loop!))

(comment
  (init!)
  (state/assoc-in-state! [:dummy] 1))

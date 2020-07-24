(ns notespace.v2.init
  (:require [notespace.v2.config :as config]
            [notespace.v2.state :as state]
            [notespace.v2.kinds :as kinds]
            [clojure.core.async :as async]
            [cambium.core :as log]
            [notespace.v2.basic-renderer :as basic-renderer]))

#_(def current-loop-id (atom nil))

#_(defn run-state-change-loop! []
  (reset! current-loop-id (rand-int 999999))
  (let [this-loop-id @current-loop-id]
    (when (= this-loop-id @current-loop-id)
      (async/go-loop []
        (let [state-change (async/<! state/state-changes)]
          (doseq [l (state/get-in-state [:state-effects])]
            (l state-change)))
        (recur)))))

#_(defn logging-state-effect! [[change-type paths-and-_]]
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
  ;; (state/assoc-in-state!
  ;;  [:state-effects] [#'logging-state-effect!
  ;;                    #'basic-renderer/effect!])
  ;; (run-state-change-loop!)
  )

(comment
  (init!)
  ;; (state/assoc-in-state! [:dummy] 1)
  )

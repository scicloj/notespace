(ns notespace.v4.events.channels
  (:require [clojure.core.async :as async :refer [<! go go-loop timeout chan]]
            [notespace.v4.log :as v4.log]
            [notespace.v4.state :as v4.state]))

(defn pass-valid-events [in out]
  (async/go-loop []
    (let [{:keys [error] :as event} (<! in)]
      (if error
        (println "Error:" error)
        ;; else -- valid
        (async/>! out event)))
    (recur)))

;; https://stackoverflow.com/a/33621605
(defn batch-events [in out {:keys [max-time max-count]}]
  (let [lim-1 (dec max-count)]
    (async/go-loop [buf []
                    t (async/timeout max-time)]
      (let [[v p] (async/alts! [in t])]
        (cond

          (= p t)
          (do
            (async/>! out buf)
            (recur [] (async/timeout max-time)))

          (nil? v)
          (when (seq buf)
            (async/>! out buf))

          (== (count buf) lim-1)
          (do
            (async/>! out (conj buf v))
            (recur [] (async/timeout max-time)))

          :else
          (recur (conj buf v) t))))))

(defn cleanup-events [in out]
  (async/go-loop []
    (->> in
         async/<!
         (group-by :type)
         (sort-by (fn [[_ {:keys [event-type]}]]
                    ({:buffer-update 1
                      :eval          2
                      :value         3} event-type)))
         (map (fn [[_ events]]
                (->> events
                     (sort-by :event-counter))))
         (mapcat (fn [events]
                   (if (-> events first :event-type (= :buffer-update))
                     [(last events)]
                     events)))
         (async/>! out))
    (recur)))

(defn handle-events [in handler]
  (async/go-loop []
    (let [events (async/<! in)]
      ;; (when (> (count events) 0)
      ;;   (v4.status/add :handle-events
      ;;                  {:n (count events)}))
      (run! handler events))
    (recur)))

(defonce events-channel (async/chan 100))

(defn start! [handler]
  (go
    (let [batched-events-channel (async/chan 20)
          clean-events-channel   (async/chan 20)]
      (batch-events events-channel batched-events-channel
                    {:max-time  200
                     :max-count 100})
      (cleanup-events batched-events-channel clean-events-channel)
      (handle-events clean-events-channel handler))))


(defn push-event [event]
  (when (v4.state/started?)
    (async/>!! events-channel
               (assoc event :event-counter (v4.state/next-event-counter)))))

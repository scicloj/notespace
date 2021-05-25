(ns notespace.v4.nrepl
  (:require [nrepl.middleware :as middleware]
            [nrepl.middleware.print :as print]
            [nrepl.transport :as transport]
            [clojure.pprint :as pp]))


(def log-path "/tmp/log.edn")

(defn log [msg]
  (spit
   log-path
   (str msg "\n")
   :append true))

(defn handle-req [request]
  (-> request
      (dissoc :session)
      pp/pprint
      with-out-str
      log))

(defn handle-send [{:keys [op] :as request}
              message]
  (when (#{"eval" "load-file"} op)
    (-> request
        (dissoc :session)
        pp/pprint
        with-out-str
        log)
    (-> message
        pp/pprint
        with-out-str
        log)))

(defn middleware [f]
  (fn [request]
    (handle-req request)
    (-> request
        (update :transport (fn [t]
                             (reify transport/Transport
                               (recv [req]
                                 (transport/recv t))
                               (recv [req timeout]
                                 (transport/recv t timeout))
                               (send [this message]
                                 (handle-send request message)
                                 (transport/send t message)
                                 this))))
        (f))))

(middleware/set-descriptor! #'middleware
                            {:requires #{#'print/wrap-print}
                             :expects #{"eval"}
                             :handles {}})

(comment
  (future (Thread/sleep 3000)
          (spit log-path ""))

  (do
    (Thread/sleep 4000)
    4))





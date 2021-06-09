(ns notespace.v4.nrepl
  (:require [nrepl.middleware :as middleware]
            [nrepl.middleware.print :as print]
            [nrepl.transport :as transport]
            [clojure.pprint :as pp]
            [clojure.core.async :as async]
            [notespace.v4.read :as v4.read]
            [notespace.v4.loop :as v4.loop]
            [notespace.v4.log :as v4.log :refer [log-data]]
            [clojure.string :as string]))

(defonce events
  (async/chan 100))

(defn get-path-when-eval-buffer [{:keys [op file-path code] :as request}]
  (cond ;;
    (= op "load-file")
    file-path
    ;;
    (and (= op "eval")
         (re-matches #".*clojure.lang.Compiler/load.*" code))
    (-> code
        read-string
        second
        second
        second
        second
        second
        first)))


(defn handle-request [{:keys [op file-path code] :as request}]
  (or (when (#{"eval" "load-file"} op)
        (some-> request
                get-path-when-eval-buffer
                v4.loop/handle-file-update))
      (when (= op "eval")
        (some-> request
                (select-keys [:column :line :code])
                v4.loop/handle-eval))))

(defn handle-message [{:keys [op] :as request}
                      message]
  (when (and (= "eval" op)
             (contains? message :value))
    (-> message
        :value
        log-data)))

(defn middleware [f]
  (fn [request]
    (handle-request request)
    (-> request
        (update :transport (fn [t]
                             (reify transport/Transport
                               (recv [req]
                                 (transport/recv t))
                               (recv [req timeout]
                                 (transport/recv t timeout))
                               (send [this message]
                                 (handle-message request message)
                                 (transport/send t message)
                                 this))))
        (f))))

(middleware/set-descriptor! #'middleware
                            {:requires #{#'print/wrap-print}
                             :expects #{"eval"}
                             :handles {}})



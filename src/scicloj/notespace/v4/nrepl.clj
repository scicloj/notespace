(ns scicloj.notespace.v4.nrepl
  (:require [nrepl.core :as nrepl]
            [nrepl.middleware :as middleware]
            [nrepl.middleware.print :as print]
            [nrepl.middleware.dynamic-loader :as dynamic-loader]
            [nrepl.transport :as transport]
            [clojure.core.async :as async]
            [scicloj.notespace.v4.events.pipeline :as v4.pipeline]
            [scicloj.notespace.v4.log :as v4.log]
            [scicloj.notespace.v4.path :as v4.path]
            [scicloj.notespace.v4.state :as v4.state]))


(defn get-path-when-eval-buffer
  [{:keys [op file-path code] :as request}]
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
        first)
    ;;
    :else
    nil))

(defn request->event [{:keys [id op file code] :as request}]
  (when (#{"eval" "load-file"} op)
    (let [path-when-eval-buffer (get-path-when-eval-buffer request)]
      (when-not (some->> file
                         (re-matches #"\*cider-repl.*\*"))
        (let [event-type (if path-when-eval-buffer
                           :scicloj.notespace.v4.events.handle/buffer-update
                           :scicloj.notespace.v4.events.handle/eval)
              path  (-> path-when-eval-buffer
                        (or file)
                        v4.path/real-path)]
          (merge {:request-id id
                  :event/type event-type
                  :path       path}
                 (when (= event-type :scicloj.notespace.v4.events.handle/eval)
                   {:code code})))))))

(defn handle-request [request]
  (some-> request
          request->event
          v4.pipeline/process-event))

(defn handle-message [{:keys [id op] :as request}
                      {:keys [value err] :as message}]
  (when (= "eval" op)
    (cond
      ;;
      (contains? message :value)
      (let [request-event (request->event request)]
        (when (-> request-event :event/type (= :scicloj.notespace.v4.events.handle/eval))
          (v4.pipeline/process-event
           {:request-id id
            :value      value
            :event/type :scicloj.notespace.v4.events.handle/value})))
      ;;
      err
      (let [request-event (request->event request)]
        (when (-> request-event :event/type (= :scicloj.notespace.v4.events.handle/eval))
          (v4.pipeline/process-event
           {:request-id id
            :err err
            :event/type :scicloj.notespace.v4.events.handle/error})))
      ;;
      (-> message :status :done)
      (let [request-event (request->event request)]
        (when (-> request-event :event/type (= :scicloj.notespace.v4.events.handle/eval))
          (v4.pipeline/process-event
           {:request-id id
            :event/type :scicloj.notespace.v4.events.handle/done}))))))

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


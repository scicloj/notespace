(ns notespace.v4.nrepl
  (:require [nrepl.middleware :as middleware]
            [nrepl.middleware.print :as print]
            [nrepl.transport :as transport]
            [clojure.core.async :as async]
            [notespace.v4.loop :as v4.loop]
            [notespace.v4.log :as v4.log]
            [notespace.v4.path :as v4.path]))


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
                           :buffer-update
                           :eval)
              path  (or path-when-eval-buffer
                        file)]
          (merge {:request-id id
                  :event-type event-type
                  :path       path}
                 (when (= event-type :eval)
                   {:code code})))))))

(defn handle-request [request]
  (some-> request
          request->event
          v4.loop/push-event))

(defn handle-message [{:keys [id op] :as request}
                      {:keys [value] :as message}]
  (when (and (= "eval" op)
             (contains? message :value))
    (let [request-event (request->event request)]
      (when (-> request-event :event-type (= :eval))
        (v4.loop/push-event
         {:request-id id
          :value      value
          :event-type :value})))))

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


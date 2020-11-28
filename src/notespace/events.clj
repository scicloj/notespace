(ns notespace.events
  (:require [cljfx.api :as fx]
            [notespace.util :as u]))

(defmulti handle :event/type)

(defmethod handle :default [event]
  (throw (ex-info "Unrecognized event"
                  {:event event})))

(defmethod handle ::reset-but-keep-config [{:keys [fx/context initial-state]}]
  {:context (fx/reset-context
             context
             (merge initial-state
                    (select-keys context [:config])))})

(defmethod handle ::update-config [{:keys [fx/context f]}]
  {:context (fx/swap-context
             context
             #(update % :config f))})

(defmethod handle ::file-modified [{:keys [fx/context namespace modification-time]}]
  {:context (fx/swap-context
             context
             #(-> %
                  (assoc-in
                   [:ns->last-modification namespace] modification-time)
                  (assoc :last-ns-handled namespace)))})

(defmethod handle ::assoc-notes [{:keys [fx/context namespace notes line->index label->indices]}]
  {:context (fx/swap-context
             context
             #(-> %
                  (assoc-in [:ns->notes namespace] notes)
                  (assoc-in [:ns->line->index namespace] line->index)
                  (assoc-in [:ns->label->indices namespace] label->indices)
                  (assoc :last-ns-handled namespace)))})

(defmethod handle ::update-note [{:keys [fx/context namespace idx f]}]
  (if idx
    (let [request-id (u/next-id :request)
          request {:request-id request-id
                   :f          f
                   :namespace  namespace
                   :idx        idx}]
      {:context (fx/swap-context
                 context
                 #(-> %
                      (assoc-in [:request-id->response request-id] {:result :pending
                                                                    :request request})
                      (assoc :last-ns-handled namespace)))
       :update  {:request request
                 :on-response {:event/type ::on-response
                               :request request
                               :result     :success}
                 :on-exception {:event/type ::on-response
                                :request request
                                :result     :failure}}})
    {:context context}))

(defmethod handle ::on-response [{:keys [fx/context request result response exception]}]
  (let [{:keys [request-id namespace idx]} request]
    {:context (fx/swap-context
               context
               #(-> %
                    (update-in [:request-id->response request-id]
                               (fn [req-status]
                                 (cond-> req-status
                                   :always             (assoc :request request
                                                              :result result)
                                   (= :success result) (assoc :response response)
                                   ;; Currently we never get here.
                                   (= :failure result) (assoc :exception exception))))
                    (update-in [:ns->notes namespace idx]
                               (if (= :success result)
                                 (constantly response)
                                 identity))))
     :throw (when (= :failure result)
              exception)}))

(defmethod handle ::assoc-input [{:keys [fx/context symbol value]}]
  {:context (fx/swap-context
             context
             #(-> %
                  (assoc-in [:inputs symbol]
                            value)))})

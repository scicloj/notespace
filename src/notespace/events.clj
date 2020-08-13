(ns notespace.events
  (:require [cljfx.api :as fx]))

(defmulti handle :event/type)

(defmethod handle :default [event]
  (throw (ex-info "Unrecognized event"
                  {:event event})))

(defmethod handle ::init [{:keys [fx/context initial-state]}]
  {:context (fx/reset-context context initial-state)})

(defmethod handle ::add-note [{:keys [fx/context note]}]
  {:context (fx/swap-context context update :notes #(conj % note))})

(defmethod handle ::realize-note [{:keys [fx/context idx]}]
  {:context     (fx/swap-context context
                                 update-in [:notes idx]
                                 #(assoc % :started-realizing true))
   :realization {:idx          idx
                 :note         (fx/sub context
                                       (fn [ctx]
                                         (-> ctx
                                             (fx/sub :notes)
                                             (get idx))))
                 :on-result    {:event/type ::on-result}
                 :on-exception {:event/type ::on-exception}}})

(defmethod handle ::on-result [{:keys [fx/context idx value]}]
  {:context (fx/swap-context context
                             assoc-in [:notes idx :value]
                             value)})

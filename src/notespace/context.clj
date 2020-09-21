(ns notespace.context
  (:require [cljfx.api :as fx]
            [notespace.state :as state]
            [notespace.events :as events]))

(defn update-note-effect [v dispatch!]
  (let [{:keys [f namespace idx]} (:request v)]
  (try
    (let [response (f (state/sub-get-in :ns->notes namespace idx))]
      (dispatch! (assoc (:on-response v) :response response)))
    (catch Exception e
      (dispatch! (assoc (:on-exception v) :exception e))))))

(defn maybe-throw [exception _]
  (when exception
    (throw exception)))

(def handle
  (-> events/handle
      (fx/wrap-co-effects
       {:fx/context (fx/make-deref-co-effect state/the-context)})
      (fx/wrap-effects
       {:context     (fx/make-reset-effect state/the-context)
        :dispatch    fx/dispatch-effect
        :update update-note-effect
        :throw maybe-throw})
      (fx/wrap-async)))

(defn mount-renderer [renderer]
  (add-watch state/the-context
             [`mount renderer]
             (fn [_ _ old-context new-context]
               (renderer old-context new-context))))

(defn unmount-renderer [renderer]
  (remove-watch state/the-context
                [`mount renderer]))

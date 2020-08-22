(ns notespace.context
  (:require [cljfx.api :as fx]
            [notespace.state :as state]
            [notespace.events :as events]))

(def handle
  (-> events/handle
      (fx/wrap-co-effects
       {:fx/context (fx/make-deref-co-effect state/the-context)})
      (fx/wrap-effects
       {:context     (fx/make-reset-effect state/the-context)
        :dispatch    fx/dispatch-effect})
      (fx/wrap-async)))

(defn mount-renderer [renderer]
  (add-watch state/the-context
             [`mount renderer]
             (fn [_ _ old-context new-context]
               (renderer old-context new-context))))

(defn unmount-renderer [renderer]
  (remove-watch state/the-context
                [`mount renderer]))

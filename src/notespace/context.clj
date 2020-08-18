(ns notespace.context
  (:require [cljfx.api :as fx]
            [clojure.core.cache :as cache]
            [notespace.events :as events]
            [notespace.effects :as effects]))

(def the-context
  (atom
   (fx/create-context {} cache/lru-cache-factory)))

(def handle
  (-> events/handle
      (fx/wrap-co-effects
       {:fx/context (fx/make-deref-co-effect the-context)})
      (fx/wrap-effects
       {:context     (fx/make-reset-effect the-context)
        :dispatch    fx/dispatch-effect
        :realization effects/realization})
      (fx/wrap-async)))

(defn mount-renderer [renderer]
  (add-watch the-context
             [`mount renderer]
             (fn [_ _ old-context new-context]
               (renderer old-context new-context))))

(defn unmount-renderer [renderer]
  (remove-watch the-context
                [`mount renderer]))

;; A conveniene function for subscribing to the value
;; at a given path inside the context.
(defn sub-get-in [& path]
  (fx/sub-val @the-context get-in path))

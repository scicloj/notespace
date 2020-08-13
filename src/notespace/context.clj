(ns notespace.context
  (:require [cljfx.api :as fx]
            [clojure.core.cache :as cache]
            [notespace.events :as events]
            [notespace.effects :as effects]))

(def *state
  (atom (fx/create-context {:notes []}
                           cache/lru-cache-factory)))

(def handle
  (-> events/handle
      (fx/wrap-co-effects
       {:fx/context (fx/make-deref-co-effect *state)})
      (fx/wrap-effects
       {:context     (fx/make-reset-effect *state)
        :dispatch    fx/dispatch-effect
        :realization effects/realization})
      (fx/wrap-async)))

(defn mount-renderer [renderer]
  (fx/mount-renderer *state renderer))

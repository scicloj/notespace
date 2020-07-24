(ns notespace.v2.kinds
  (:require [notespace.v2.state :as state]
            [notespace.v2.view :as view]
            [hiccup.core :as hiccup]))

(defn define-kind! [kind behaviour]
  (state/assoc-in-state!
   [:kind->behaviour ~kind] ~behaviour
   [:note-symbol->kind (quote ~note-symbol)] ~kind))

(defn define-base-kinds! []
  (define-kind!
    :code {:render-src?    true
           :value-renderer view/value->hiccup})
  (define-kind!
    :md   {:render-src?    false
           :value-renderer view/md->hiccup})
  (define-kind!
    :as-md   {:render-src?    true
              :value-renderer view/md->hiccup})
  (define-kind!
    :hiccup {:render-src?    false
             :value-renderer (fn [h] (hiccup/html h))})
  (define-kind!
    :as-hiccup {:render-src?    true
                :value-renderer (fn [h] (hiccup/html h))})
  (define-kind!
    :void {:render-src?    true
           :value-renderer (constantly nil)}))


(ns notespace.kinds
  (:require [notespace.view :as view]))

(defmulti kind->behaviour identity)

(defmethod kind->behaviour ::naive
  [_]
  {:render-src?   true
   :value->hiccup #'view/value->naive-hiccup})

(defmethod kind->behaviour ::md-nocode
  [_]
  {:render-src?   false
   :value->hiccup #'view/markdowns->hiccup})

(defmethod kind->behaviour ::md
  [_]
  {:render-src?   true
   :value->hiccup #'view/markdowns->hiccup})

(defmethod kind->behaviour ::hiccup-nocode
  [_]
  {:render-src?   false
   :value->hiccup identity})

(defmethod kind->behaviour ::hiccup
  [_]
  {:render-src?   true
   :value->hiccup identity})

(defmethod kind->behaviour ::vega
  [_]
  {:render-src?   true
   :value->hiccup (partial vector :p/vega)})

(defmethod kind->behaviour ::void
  [_]
  {:render-src?   true
   :value->hiccup (constantly nil)})

(defmethod kind->behaviour ::hidden
  [_]
  {:render-src?   false
   :value->hiccup (constantly nil)})

(defmethod kind->behaviour ::dataset-grid
  [_]
  {:render-src?   true
   :value->hiccup #'view/dataset->grid-hiccup})

(defmethod kind->behaviour ::dataset
  [_]
  {:render-src?   true
   :value->hiccup #'view/dataset->md-hiccup})

(defn intern-kinds! []
  (doseq [m (map first (seq (methods kind->behaviour)))]
    (intern *ns* (symbol (name m)) m)))

(intern-kinds!)

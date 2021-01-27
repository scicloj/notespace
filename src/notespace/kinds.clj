(ns notespace.kinds
  (:require [notespace.view :as view]))

(declare kind->behaviour)
(ns-unmap *ns* 'kind->behavior)
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

(defmethod kind->behaviour ::midje
  [_]
  {:render-src?   true
   :value->hiccup #'view/test-boolean->hiccup})

(defmethod kind->behaviour ::html
  [_]
  {:render-src?   true
   :value->hiccup (partial vector :p/html)})

(defn intern-kinds! []
  (doseq [m (map first (seq (methods kind->behaviour)))]
    (intern *ns* (symbol (name m)) m)))

(intern-kinds!)

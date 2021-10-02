(ns scicloj.notespace.v4.kinds
  (:require [notespace.view :as view]))

(declare kind->behavior)
(ns-unmap *ns* 'kind->behavior)
(defmulti kind->behavior identity)

(defmethod kind->behavior :notespace.kinds/naive
  [_]
  {:render-src?   true
   :value->hiccup #'view/value->naive-hiccup})

(defmethod kind->behavior :notespace.kinds/md-nocode
  [_]
  {:render-src?   false
   :value->hiccup #'view/markdowns->hiccup})

(defmethod kind->behavior :notespace.kinds/md
  [_]
  {:render-src?   true
   :value->hiccup #'view/markdowns->hiccup})

(defmethod kind->behavior :notespace.kinds/hiccup-nocode
  [_]
  {:render-src?   false
   :value->hiccup identity})

(defmethod kind->behavior :notespace.kinds/hiccup
  [_]
  {:render-src?   true
   :value->hiccup identity})

(defmethod kind->behavior :notespace.kinds/vega
  [_]
  {:render-src?   true
   :value->hiccup (partial vector :p/vega)})

(defmethod kind->behavior :notespace.kinds/quil
  [_]
  {:render-src?   true
   :value->hiccup (partial vector :p/quil)})

(defmethod kind->behavior :notespace.kinds/sci
  [_]
  {:render-src?   true
   :value->hiccup (partial vector :p/sci)})

(defn wrap [wrapper v]
  (if (vector? v)
    (->> v
         (map (fn [x]
                [wrapper x]))
         (into [:div]))
    [wrapper v]))

(defmethod kind->behavior :notespace.kinds/math
  [_]
  {:render-src?   true
   :value->hiccup (partial wrap :p/math)})

(defmethod kind->behavior :notespace.kinds/code
  [_]
  {:render-src?   true
   :value->hiccup (partial wrap :p/code)})

(defmethod kind->behavior :notespace.kinds/void
  [_]
  {:render-src?   true
   :value->hiccup (constantly nil)})

(defmethod kind->behavior :notespace.kinds/hidden
  [_]
  {:render-src?   false
   :value->hiccup (constantly nil)})

(defmethod kind->behavior :notespace.kinds/dataset-grid
  [_]
  {:render-src?   true
   :value->hiccup #'view/dataset->grid-hiccup})

(defmethod kind->behavior :notespace.kinds/dataset
  [_]
  {:render-src?   true
   :value->hiccup #'view/dataset->md-hiccup})

(defmethod kind->behavior :notespace.kinds/clojure-test
  [_]
  {:render-src?   true
   :value->hiccup #'view/test-boolean->hiccup})

(defmethod kind->behavior :notespace.kinds/midje
  [_]
  {:render-src?   true
   :value->hiccup #'view/test-boolean->hiccup})

(defmethod kind->behavior :notespace.kinds/html
  [_]
  {:render-src?   true
   :value->hiccup (partial vector :p/html)})

(defn intern-kinds! []
  (doseq [m (map first (seq (methods kind->behavior)))]
    (intern *ns* (symbol (name m)) m)))

(intern-kinds!)

(defn override [value kind]
  (vary-meta value assoc :notespace.kind kind))

(defprotocol Kindness
  (->behavior [this]))

(extend-protocol Kindness
  Object
  (->behavior [this]
    nil))

(defn kinds-set []
  (into #{} (map first) (-> kind->behavior methods seq)))

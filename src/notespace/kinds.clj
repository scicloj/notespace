(ns notespace.kinds
  (:require [notespace.view :as view]))

(def kind->behaviour
  {:naive     {:render-src?   true
               :value->hiccup #'view/value->naive-hiccup}
   :md        {:render-src?   false
               :value->hiccup #'view/markdowns->hiccup}
   :as-md     {:render-src?   true
               :value->hiccup #'view/markdowns->hiccup}
   :hiccup    {:render-src?   false
               :value->hiccup identity}
   :as-hiccup {:render-src?   true
               :value->hiccup identity}
   :void      {:render-src?   true
               :value->hiccup (constantly nil)}
   :hidden    {:render-src?   false
               :value->hiccup (constantly nil)}})

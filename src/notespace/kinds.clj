(ns notespace.kinds
  (:require [notespace.view :as view]))

(def kind->behaviour
  {::naive         {:render-src?   true
                    :value->hiccup #'view/value->naive-hiccup}
   ::md-nocode     {:render-src?   false
                    :value->hiccup #'view/markdowns->hiccup}
   ::md            {:render-src?   true
                    :value->hiccup #'view/markdowns->hiccup}
   ::hiccup-nocode {:render-src?   false
                    :value->hiccup identity}
   ::hiccup        {:render-src?   true
                    :value->hiccup identity}
   ::vega          {:render-src?   true
                    :value->hiccup (partial vector :p/vega)}
   ::void          {:render-src?   true
                    :value->hiccup (constantly nil)}
   ::hidden        {:render-src?   false
                    :value->hiccup (constantly nil)}})

(def naive ::naive)
(def md-nocode ::md-nocode)
(def md ::md)
(def hiccup-nocode ::hiccup-nocode)
(def hiccup ::hiccup)
(def vega ::vega)
(def void ::void)
(def hidden ::hidden)

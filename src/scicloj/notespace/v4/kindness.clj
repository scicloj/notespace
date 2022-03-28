(ns scicloj.notespace.v4.kindness
  (:require [scicloj.kindly.v1.kindness :as kindness]
            [scicloj.kindly.v1.kind :as kind]
            [scicloj.kindly.v1.api :as kindly]
            [scicloj.notespace.v4.image :as image]))

(extend-protocol kindness/Kindness
  ;;
  java.awt.image.BufferedImage
  (->behaviour [this]
    {:render-src?   true
     :value->hiccup image/buffered-image->hiccup})
  ;;
  clojure.lang.Delay
  (->behaviour [this]
    (let [realized (deref this)
          realized-behaviour (-> this
                                 deref
                                 kindly/value->behaviour
                                 (or (kindly/kind->behaviour
                                      kind/naive)))]
      {:render-src? (-> realized-behaviour
                       :render-src?)
       :value->hiccup (-> realized-behaviour
                          :value->hiccup
                          (comp deref))})))

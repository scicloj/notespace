(ns scicloj.notespace.v4.kindness
  (:require [scicloj.kindly.kindness :as kindness]
            [scicloj.notespace.v4.image :as image])
  (:import java.awt.image.BufferedImage))

(extend-protocol kindness/Kindness
  BufferedImage
  (->behaviour [this]
    {:render-src?   true
     :value->hiccup image/buffered-image->hiccup}))

(ns notespace.v4.render
  (:require [notespace.v4.image :as v4.image]))


(defprotocol Renderable
  :extend-via-metadata true
  (render [this]))

(extend-protocol Renderable
  Object
  (render [o]
    [:p/code
     (pr-str o)])
  nil
  (render [this]
    [:div])
  java.awt.image.BufferedImage
  (render [this]
    (v4.image/buffered-image->hiccup this)))

(defn as-hiccup [content]
  (vary-meta
   content
   assoc 'notespace.v4.render/render identity))


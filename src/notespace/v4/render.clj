(ns notespace.v4.render)


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
    [:div]))


(defn as-hiccup [content]
  (vary-meta
   content
   assoc 'notespace.v4.render/render identity))


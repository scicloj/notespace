(ns notespace.v2.css
  (:require [clojure.java.io :as io]))

;; read css from resource with name same as keyword or inline provided text
(defn include-css [keyword-or-css-string]
  [:style {:type "text/css"}
   (if (keyword? keyword-or-css-string)
     (if-let [css (->> (name keyword-or-css-string)
                       (format "css/%s.css")
                       (io/resource))]
       (slurp css)
       (include-css :basic))
     keyword-or-css-string)])

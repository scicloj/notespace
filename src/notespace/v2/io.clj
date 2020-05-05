(ns notespace.v2.io
  (:require [clojure.java.browse :as browse])
  (:import (java.io File)))

(defn view-html! [h]
  (let [f (str (File/createTempFile "view" ".html"))]
    (spit f h)
    (future (browse/browse-url f))))

(defn make-path [path]
  (.mkdirs ^File (File. path))
  path)

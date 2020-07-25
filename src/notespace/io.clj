(ns notespace.v2.io
  (:require [clojure.java.browse :as browse]
            [clojure.java.io :as io])
  (:import (java.io File)))

(defn view-html! [h]
  (let [f (str (File/createTempFile "view" ".html"))]
    (spit f h)
    (future (browse/browse-url f))))

(defn make-path [path]
  (.mkdirs ^File (File. path))
  path)

(defn copy
  "Download or a file -- see https://stackoverflow.com/a/19297746/1723677"
  [uri file]
  (with-open [in  (io/input-stream uri)
              out (io/output-stream file)]
    (io/copy in out)))

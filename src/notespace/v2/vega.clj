(ns notespace.v2.vega
  (:require
   [hickory.core :as h]
   [clojure.data.json :as json]
   [clojure.java.shell :as sh]
   [vg-cli.core]
   )

  )



(defn vega->hiccup [spec mode]
  "Converts a vega lite specification file into hiccup, which can be rendered via note-hiccup

     spec - a vega / vega-lite spec
     mode - either :vega or :vega-lite

  "
  (first
   (map h/as-hiccup
        (h/parse-fragment
         (vg-cli.core/vg-cli {:spec spec :mode mode}))))
  )






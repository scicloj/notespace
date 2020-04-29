(ns notespace.v2.vega
  (:require
   [hickory.core :as h]
   [applied-science.darkstar :as darkstar]
   [jsonista.core :as jsonista]))

(defn vega->svg [spec mode]
  "Converts a vega lite specification file into hiccup, which can be rendered via note-hiccup

     spec - a vega / vega-lite spec
     mode - either :vega or :vega-lite"
  (let [json (jsonista/write-value-as-string spec)]
    (-> (case mode
          :vega      (darkstar/vega-spec->svg json)
          :vega-lite (darkstar/vega-lite-spec->svg json))
        h/parse-fragment
        first
        h/as-hiccup)))

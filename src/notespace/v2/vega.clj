(ns notespace.v2.vega
  (:require
   [hickory.core :as h]
   [clojure.data.json :as json]
   [clojure.java.shell :as sh]

   )

  )


(defn temp-json-file
  "helper function convert clj vega spec to json and store it as tmp file"
  [content]
  (let [tmp-file (java.io.File/createTempFile "vega." ".json")]
    (.deleteOnExit tmp-file)
    (with-open [file (clojure.java.io/writer tmp-file)]
      (json/write content file))
    (.getAbsolutePath tmp-file)))


(defn vega->hiccup [vega-lite type]
  "Converts a vega lite specification file into hiccup, which can be rendered via note-hiccup"
  (let [temp-vega-file (java.io.File/createTempFile "plot." ".vega")
        executable (if (= :vega type)
                     "vg2svg"
                     "vl2svg"
                     )
        _ (.deleteOnExit temp-vega-file)
        temp-vega-file-name (.getAbsolutePath temp-vega-file)
        result (sh/sh executable (temp-json-file vega-lite) temp-vega-file-name)
        hiccup (first (map h/as-hiccup (h/parse-fragment (slurp temp-vega-file-name))))
        ]
    hiccup)
  )



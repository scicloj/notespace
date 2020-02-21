(ns notespace.v2.reader
  (:require [clojure.tools.reader :as tr]
            [clojure.tools.reader.reader-types :as rts]))

(defn file->topforms-with-metadata [path]
  (->> path
       slurp
       rts/source-logging-push-back-reader
       repeat
       (map #(tr/read % false :EOF))
       (take-while (partial not= :EOF))))

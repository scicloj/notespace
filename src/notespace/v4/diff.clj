(ns notespace.v4.diff
  (:require [editscript.core :as editscript]
            [notespace.v4.metadata :as v4.metadata]))

(defn diff-by-function [old-values new-values f]
  (->> [old-values new-values]
       (map (fn [values]
              (->> values
                   (mapv
                    (fn [value]
                      (v4.metadata/->HasMeta
                       {::f-value (f value)}
                       {::value value}))))))
       (apply editscript/diff)
       editscript/get-edits
       (mapv (fn [[path op & args]]
               (let [arg (first args)]
                 [path op arg]
                 (cond (nil? arg)    [path op]
                       (vector? arg) [path op (mapv
                                               #(-> %
                                                    meta
                                                    ::value)
                                               arg)]
                       :else         [path op (-> arg
                                          meta
                                          ::value)]))))))

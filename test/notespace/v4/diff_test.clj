(ns notespace.v4.diff-test
  (:require [notespace.v4.diff :as v4.diff]
            [editscript.core :as editscript]
            [midje.sweet :refer [fact =>]]))

(fact (let [old-values [{:abcd 1 :efgh 1}
                        {:abcd 2 :efgh 1}
                        {:abcd 3 :efgh 1}]
            new-values [{:abcd 1 :efgh 2}
                        {:abcd 4 :efgh 2}
                        {:abcd 3 :efgh 2}]
            edits      (v4.diff/diff-by-function old-values
                                            new-values
                                            :abcd)]
        ;;;;
        edits
        =>
        [[[1] :r {:abcd 4 :efgh 2}]]
        ;;;;
        (editscript/patch old-values
                          (editscript/edits->script edits))
        =>
        [{:abcd 1 :efgh 1}
         {:abcd 4 :efgh 2}
         {:abcd 3 :efgh 1}]))


(fact (let [old-values []
            new-values [{:abcd 1 :efgh 2}
                        {:abcd 4 :efgh 2}
                        {:abcd 3 :efgh 2}]
            edits      (v4.diff/diff-by-function old-values
                                                 new-values
                                                 :abcd)]
        ;;;;
        edits
        =>
        [[[] :r [{:abcd 1, :efgh 2}
                 {:abcd 4, :efgh 2}
                 {:abcd 3, :efgh 2}]]]
        ;;;;
        (editscript/patch old-values
                          (editscript/edits->script edits))
        =>
        [{:abcd 1 :efgh 2}
         {:abcd 4 :efgh 2}
         {:abcd 3 :efgh 2}] ))


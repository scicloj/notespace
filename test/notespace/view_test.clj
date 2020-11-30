(ns notespace.view-test
  (:require [notespace.view :as view]
            [midje.sweet :refer [with-state-changes => fact facts before]]

            [clojure.test :refer :all]))


(fact "map-of-seqs converts to grid"
      (->
       (view/dataset->grid-hiccup
        {:a [1 2 3]
         :b [4 5 6] })
       (nth 3)
       (second)
       :rowData) =>
      [{"a" 1 "b" 4}
       {"a" 2 "b" 5}
       {"a" 3 "b" 6}])

(fact "seq-of-maps converts to grid"
      (->
       (view/dataset->grid-hiccup
        [{:a 1 :b 4}
         {:a 2 :b 5}
         {:a 3 :b 6}])
       (nth 3)
       (second)
       :rowData) =>
      [{"a" 1 "b" 4}
       {"a" 2 "b" 5}
       {"a" 3 "b" 6}])

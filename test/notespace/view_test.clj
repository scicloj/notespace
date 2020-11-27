(ns notespace.view-test
  (:require [notespace.view :as view]
            [clojure.test :refer :all]))


(deftest test-map-of-seqs
  (is (= [{"a" 1 "b" 4}
          {"a" 2 "b" 5}
          {"a" 3 "b" 6}]
         (->
          (view/dataset->grid-hiccup
           {:a [1 2 3]
            :b [4 5 6] })
          (nth 3)
          (second)
          :rowData))))

(deftest test-seq-of-maps
  (is (= [{"a" 1 "b" 4}
          {"a" 2 "b" 5}
          {"a" 3 "b" 6}]
         (->
          (view/dataset->grid-hiccup
           [{:a 1 :b 4}
            {:a 2 :b 5}
            {:a 3 :b 6}])
          (nth 3)
          (second)
          :rowData))))

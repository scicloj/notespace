(ns notespace.view-test
  (:require [notespace.view :as view]
            [notespace.note :as note]
            [notespace.api :as api]
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

(def simple-note
  (first  (note/ns-notes "notespace.simple-notespace-test")))

(with-state-changes [(before :facts
                             (api/update-config #(assoc % :render-src? true)))]
  (fact ""
        (-> (view/note->hiccup simple-note)
            second) => [:p/code  { :code "(+ 1 1)", :bg-class "bg-light" }]))

(with-state-changes [(before :facts
                             (api/update-config #(assoc % :render-src? false)))]
  (fact ""
        (-> (view/note->hiccup simple-note)
            second) => nil))

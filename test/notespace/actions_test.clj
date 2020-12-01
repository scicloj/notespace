(ns notespace.actions-test
  (:require [notespace.actions :as sut]
            [notespace.state :as state]
            [notespace.lifecycle :as lifecycle]
            [midje.sweet :refer :all]))


(with-state-changes
  [(before :facts (do
                    (lifecycle/init)))]


  (facts "can evaluate all nodes in test-ns"

        (sut/act-on-notes! "notespace.all-kinds-notespace-test" [sut/eval-note!])
        (let [all-notes
              (state/sub-get-in :ns->notes "notespace.all-kinds-notespace-test")]
          (fact "all are evaluated"
                (->> all-notes
                     (map :status)
                     frequencies) => {{:stage :evaluated} 76
					{:stage :initial} 1})
          (fact "only 5 are evaluated to nil"
                (->> all-notes
                     (map :value)
                     (filter nil?)
                     count) => 5))))

(ns notespace.actions-test
  (:require [notespace.actions :as sut]
            [notespace.state :as state]
            [notespace.kinds :as k]
            [notespace.view :as view]
            [notespace.api :as api]
            [notespace.setup-test]
            [midje.sweet :refer :all]))


(defmethod k/kind->behaviour ::big
  []
  {:render-src?   true
   :value->hiccup #'view/value->naive-hiccup})


;; (with-state-changes
;;   [(before :facts (do
;;                     (api/init :port notespace.setup-test/testing-port)))]


;;   (facts "can evaluate all nodes in test-ns"

;;          (sut/act-on-notes! "notespace.all-kinds-notespace-test" [sut/eval-note!])
;;          (let [all-notes
;;                (state/sub-get-in :ns->notes "notespace.all-kinds-notespace-test")]
;;            (fact "all are evaluated"
;;                  (->> all-notes
;;                       (map :stage)
;;                       frequencies) => {:evaluated 76 :initial 1 })
;;            (fact "only 5 are evaluated to nil"
;;                  (->> all-notes
;;                       (map :value)
;;                       (filter nil?)
;;                       count) => 5))))



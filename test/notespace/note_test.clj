(ns notespace.note-test
  (:require [notespace.note :as sut]
            [notespace.kinds :as kind]
            [notespace.lifecycle :as lifecycle]
            [notespace.api :as api]
            [notespace.renderers.gorilla-notes :as gn]
            [midje.sweet :refer [with-state-changes => fact facts before]]
            [midje.checkers :refer [roughly]]
            ))

(defn  sleep-one-sec []
  (Thread/sleep 1000)
  )

(def sleep-note
  (sut/->Note kind/naive "" '((sleep-one-sec)) nil nil nil nil))


(def fn-params (atom nil))

(with-state-changes
  [(before :facts (do
                    (reset! fn-params nil)
                    (lifecycle/init)
                    (api/update-config
                     #(assoc % :progress-render-fn
                             (fn [idx count duration]
                               (reset! fn-params {:idx idx :count count :duration duration}))))))]

  (fact "evaluating note sets duration"
        (:duration
         (sut/evaluated-note "notespace.empty-notespace-test" 0 sleep-note))
        => (roughly 1000 100)
        )

  (fact "evaluating note call progress-render-fn"
        (:duration
         (sut/evaluated-note "notespace.empty-notespace-test" 0 sleep-note))
        @fn-params => {:count 0 :duration 0.0 :idx 0}
        )
  )

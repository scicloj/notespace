(ns notespace.note-test
  (:require [notespace.note :as sut]
            [notespace.kinds :as kind]
            [notespace.api :as api]
            [notespace.renderers.gorilla-notes :as gn]
            [midje.sweet :refer [with-state-changes => fact facts before truthy]]
            [midje.checkers :refer [roughly]]))

(defn  sleep-one-sec []
  (Thread/sleep 1000))

(def sleep-note
  (->
   (sut/->Note kind/naive "" '((sleep-one-sec)) nil nil nil nil)
   (assoc :duration 100)
   ))

(def note-with-duration
  (->
   (sut/->Note kind/naive "" '((+ 1 1)) nil nil nil nil)
   (assoc :duration 2000)
   ))


(def fn-params-evaluation-callback (atom nil))
(def fn-params-in-eval-count-down-fn  (atom nil))

(with-state-changes
  [(before :facts (do
                    (reset! fn-params-evaluation-callback nil)
                    (api/init)
                    (api/update-config
                     #(assoc % :evaluation-callback-fn
                             (fn [idx count note]
                               (reset! fn-params-evaluation-callback {:idx idx :count count :note note}))))
                    (api/update-config
                     #(assoc % :in-eval-count-down-fn
                             (fn [idx]
                               (reset! fn-params-in-eval-count-down-fn {:idx idx}))))))]

  (fact "evaluating note sets duration"
        (get-in
         (sut/evaluated-note "notespace.empty-notespace-test" 0 sleep-note)
         [:duration]
         )
        => (roughly 1000 100))

  (fact "evaluating note call progress-render-fn"

        (sut/evaluated-note "notespace.empty-notespace-test" 0 sleep-note)
        (Thread/sleep 500)
        (:idx @fn-params-evaluation-callback) => 0
        (get-in @fn-params-evaluation-callback [:note :duration]) => 100)

  (fact "evaluating note call in-eval-fn"
        (sut/evaluated-note "notespace.empty-notespace-test" 0 note-with-duration)
        (Thread/sleep 2000)
        @fn-params-in-eval-count-down-fn => {:idx 0})

  (fact "evaluation of note runs code"
        (let [note-list
              (sut/ns-notes "notespace.all-kinds-notespace-test")]
          (:value
           (sut/evaluated-note "notespace.all-kinds-notespace-test" 4 (nth note-list 4) ))
          => 21)))


(facts "Can convert test file to notes"
       (let [note-list
             (sut/ns-notes "notespace.all-kinds-notespace-test")
             a-naive-node (first note-list)]
         (fact "all are converted and in initial state"
               (first (distinct (map :stage note-list))) => :initial
               (first (distinct (map :realized-value note-list))) => nil
               (first (distinct (map :value note-list))) => :value/not-ready
               (first (distinct (map :label note-list))) => nil
               (->> (map :forms note-list)
                    (remove nil?)
                    count
                    ) => 77
               (count note-list) => 77
               (frequencies (map :kind note-list) ) =>
               {:notespace.kinds/naive 20
                :notespace.kinds/big 3
                :notespace.kinds/md-nocode 44
                :notespace.kinds/hiccup 1
                :notespace.kinds/div 1
                :notespace.kinds/leafletmap 1
                :notespace.kinds/void 5
                :notespace.kinds/code 1
                :notespace.kinds/player 1 })
         (fact "a naive node has a form and metadata"
               (-> (:forms a-naive-node)
                   first
                   first
                   ) => 'comment
               (let [metadata (:metadata a-naive-node)]
                 (:source metadata) => truthy
                 (dissoc metadata :source) =>  {  :line  7
                                                :column  1
                                                :end-line  12
                                                :end-column  76
                                                :tag 'k/hidden}))))


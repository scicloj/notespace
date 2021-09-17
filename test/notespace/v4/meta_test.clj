(ns notespace.v4.meta-test
  (:require [notespace.v4.meta :as v4.meta]
            [midje.sweet :refer [fact =>]]))

(fact (= (v4.meta/->HasMeta "abcd" {})
         (v4.meta/->HasMeta "abcd" {:efgh 1}))
      => true)

(fact (not= (v4.meta/->HasMeta "abcd" {})
            (v4.meta/->HasMeta "abcde" {}))
      => true)

(fact (-> (v4.meta/->HasMeta "abcd" {:efgh 1})
          meta)
      => {:efgh 1})

(fact (-> (v4.meta/->HasMeta "abcd" {:efgh 1})
          (vary-meta assoc :ijkl 2)
          meta)
      => {:efgh 1
          :ijkl 2})





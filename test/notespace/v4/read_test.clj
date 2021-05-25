(ns notespace.v4.read-test
  (:require [notespace.v4.read :as v4.read]
            [midje.sweet :refer [fact =>]]))


(fact (v4.read/->notes
       "
(def x
  ;; let us define x
  9)

(comment
  (+ 1 2))

;; ok?
;; ok.

3

\"hi!\"

false

x

3 4 5

:bye
")

      =>

      [{:region [2 1 4 5], :source "(def x\n  ;; let us define x\n  9)"}
       {:region [6 1 7 11], :source "(comment\n  (+ 1 2))"}
       {:region [9 1 10 7], :source ";; ok?\n;; ok.", :comment? true}
       {:region [12 1 12 2], :source "3"}
       {:region [14 1 14 6], :source "\"hi!\""}
       {:region [16 1 16 6], :source "false"}
       {:region [18 1 18 2], :source "x"}
       {:region [20 1 20 2], :source "3"}
       {:region [20 3 20 4], :source "4"}
       {:region [20 5 20 6], :source "5"}
       {:region [22 1 22 5], :source ":bye"}])

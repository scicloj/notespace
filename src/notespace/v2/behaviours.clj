(ns notespace.v2.behaviours)

;; A note's kind determintes controls various parameters of its evaluation and rendering.
(def kind->behaviour
  (atom {}))


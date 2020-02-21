(ns notespace.v1.behaviours)

;; A note's kind determintes controls various parameters of its evaluation and rendering.
(def kind->behaviour
  (atom {}))


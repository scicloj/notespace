;; # Notespace tutorial

(ns scicloj.notespace.v4.tutorial-test
  (:require [scicloj.notespace.v4.api :as notespace] ; the Notespace API
            [scicloj.kindly.v1.api :as kindly] ; specifying kinds of notes
            [scicloj.kindly.v1.kind :as kind] ; a collection of known kinds of notes
            [scicloj.kindly.v1.kindness :as kindness]))

;; ## (re)starting Notespace

;; To start or restart the Notespace system, use `restart!`:

(comment
  (notespace/restart!))

;; You should see the following message printed in the REPL:
;; ```
;; Server starting...
;; Ready at port 1903 .
;; ```
;; Then you can browse [http://localhost:1903](http://localhost:1903) for the Notespace browser view.
;;

;; ### restart options

;; To use an alternative port:


(comment
  (notespace/restart! {:port 1904}))

;; To automatically open a browser tab on restart:

(comment
  (notespace/restart! {:open-browser? true}))

;; ## The browser view

;; The Notespace browser view contains two parts:
;; * The header contains an events log, some metadata, and the last evaluation value.
;; * The body contains your code, intertwined with evaluation values.

;; Top-level text comments are rendered as markdown (including LaTeX formulae: $e^{\pi i}+1=0$).
;;
;; Top-level forms (so-called "notes") are rendered by their specified "kind". More on that -- below.

;; ## Interaction

;; Notespace listens to file-save events and to code evaluations in your editor/REPL environment.

;; * A file save or an evaluation of a whole file will result in updating the viewed code.

;; * An evaluation of a region will result in updating the last evaluation value, as well as the values of the evaluated forms, if they can be recognized in the code unambiguously.

;; (Sometimes, Notespace does not know how to recognize the evaluated forms unambiguously in your editor's buffer. For example, maybe it appears more than once, maybe the file hasn't been saved since some recent changes, and maybe the evaluated region is not a set of top-level forms.)

;; ## Static rendering

;; To save your notebook's body as a static file:

(comment
  (notespace/render-as-html! "/tmp/notespace/index.html"))

;; This functionality needs some more care to become more convenient when working with multiple file.

;; ## Basic examples

(+ 1 2)

(def x 9)

(rand)

;; ## Specifying note kinds

;; The notion of note kinds is very similar to the one we had at v3. This needs to be documented more carefully, but for now, here are a few examples.

;; ### by a metadata tag at the source code

^kind/hiccup
[:p/sparklinespot
 {:data      (->> #(- (rand) 0.5)
                  (repeatedly 99)
                  (reductions +))
  :svgHeight 50}]

;; ### by varying the metadata of the returned value

(-> {:description "A simple bar chart with embedded data."
     :height 50
     :data        {:values [{:a "A" :b 28} {:a "B" :b 55} {:a "C" :b 43}
                            {:a "D" :b (+ 91 (rand-int 9))} {:a "E" :b 81} {:a "F" :b 53}
                            {:a "G" :b 19} {:a "H" :b 87} {:a "I" :b 52}]}
     :mark        :bar
     :encoding    {:x {:field :a :type :nominal :axis {:labelAngle 0}}
                   :y {:field :b :type :quantitative}}}
    (kindly/consider kind/vega))

;; ### by implementing the Kindness protocol

(deftype BigBigBigText [text]
  kindness/Kindness
  (->behaviour [this]
    {:render-src?   true
     :value->hiccup (fn [value]
                      [:big [:big [:big (.text value)]]])}))

(BigBigBigText. "hi!")

;; ## Delays

;; When the evaluation value is a Clojure [delay](https://clojuredocs.org/clojure.core/delay), will render by dereferencing the delay.

;; We encourage the user to put slow computations in `delay` blocks. This way, evaluating the whole namespace is fast, and slowness is experienced only in the context of evaluating specific parts of it for rendering.

(delay
  (Thread/sleep 500)
  (+ 1 2))

;; ## Troubleshooting

;; Notespace my run into bugs and unreliable states.

;; One useful practice in such a situation is restarting its event system:

(comment
  (notespace/restart-events!))

;; A more complete restart would be restarting the whole Notespace, including the webserver.

(comment
  (notespace/restart!))

;; After this kind of complete restart, a browser refresh will also be needed.

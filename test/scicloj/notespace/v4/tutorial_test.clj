(ns scicloj.notespace.v4.tutorial-test
  (:require [scicloj.notespace.v4.api :as notespace] ; the Notespace API
            [scicloj.kindly.api :as kindly] ; specifying kinds of notes
            [scicloj.kindly.kind :as kind] ; a collection of known kinds of notes
            ))

;; ## How to (re)start?

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

;; ## What do we see?

;; The Notespace view contains two parts:
;; * The header contains an events log, some metadata, and the last evaluation value.
;; * The body contains your code, intertwined with evaluation values.

;; Top-level text comments are rendered as markdown. Top-level forms (so-called "notes") are rendered by their specified "kind". More on that -- below.

;; ## How to interact?

;; Notespace listens to file-save events and to code evaluations in your editor/REPL environment.

;; * A file save or an evaluation of a whole file will result in updating the viewed code.

;; * An evaluation of a region will result in updating the last evaluation value, as well as the values of the evaluated forms, if they can be recognized in the code unambiguously.

;; (Sometimes, Notespace does not know how to recognize the evaluated forms unambiguously in your editor's buffer. For example, maybe it appears more than once, maybe the file hasn't been saved since some recent changes, and maybe the evaluated region is not a set of top-level forms.)

;; ## Static rendering

;; To save your notebook's body as a static file:

(comment
  (notespace/render-as-html! "/tmp/notespace/index.html"))

;; ## Basic examples

(+ 1 2)

(def x 9)

(rand)

;; ## Specifying note kinds

;; Coming soon

;; ## Troubleshooting

;; Notespace my run into bugs.

;; One useful practice in such a situation is restarting its event system:

(comment
  (notespace/restart-events!))

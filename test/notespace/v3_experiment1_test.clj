(ns notespace.v3-experiment1-test
  (:require [notespace.api :as api]
            [notespace.kinds :as k]
            [clojure.java.browse :as browse]))

^k/hidden
(comment
  (api/init-with-browser)

  (api/init)

  (api/eval-this-notespace)

  (api/render-static-html)

  (browse/browse-url
   "doc/notespace/v3-experiment1-test/index.html"))

["# Notespace v3 intro

[Notespace](https://github.com/scicloj/notespace) is a library that turns any Clojure namespace into a 'notespace' -- a namespace with a [notebook](https://en.wikipedia.org/wiki/Notebook_interface) experience. A notespace evolves in an interactive process of exploration, involving a dialogue of your editor, your REPL, your browser and your mind.

So, `Notespace` with a capital `N` is the name of the tool, and `notespace` with a small `n` is the notion that we just presented.

Here we describe an experimental incomplete draft of Notespace Version 3.

It is still a bit buggy around handling concurrency, updating efficiently and connecting to the browser. But the current version should be enough to start a community discussion around where we should be taking this."]

["## Notes

Here is a note:"]

(+ 1 2
   3 4
   5 6)

["Notespace renders it so that we see both the code and the evaluation result.

All your namespace is seen as a sequence of toplevel forms, which are considered as notes, and rendered accordingly."]

["## Basic workflow

Typically, you may like to edit one or more namespaces, that may be about function definitions, tests, data exploration, etc., and have Notespace update its notebook view while these namespaces evolve. At some point, you may like to render the view as a standalone html file for documentation.

See [this screencast](https://www.youtube.com/watch?v=_GcTnkhn9g0) for a recorded session. Note, however, that the note kinds defined in the screencast are a bit different from those of the current version.

Let us see how to do this with the Notespace API."

"### The main API functions"

"#### init

Use `notespace.api/init` to initialize the Notespace system's state.

In the beginning of your work, you need to use it.

You can also use it afterwards. That is useful if you run into an unstable state (yes, this may happen sometimes -- there may be some concurrency bugs)."

"#### init-with-browser

Use `notespace.api/init-with-browser` to initialize the Notespace system's state and open a browser tab with a notebook view of the last namespace touched.

In the beginning of your work, you can use it instead of `init`.

You can always refersh the browser tab. That is useful if it runs out of sync with the system's state (yes, this may happen sometimes -- there may be some concurrency bugs)."

"#### eval-this-notespace

Use `notespace.api/eval-this-notespace` to evaluate the current namespace, one note after another, and inform Notespace about the evaluation results, one after another.

If you have a browser view open, it should show the updated rendering of the state of the notespace last touched."

"#### eval-note-at-line

Use `notespace.api/eval-note-at-line` to evaluate the note at a certain line and inform Noteapace about the evaluation result.

For example, `(notespace.api/eval-note-at-line 14)` applies that to the note at line 14.

If you have a browser view open, it should show the updated rendering of the state of the notespace last touched.

This is useful for re-evaluating parts of the notebook, whose evaluation result may have changed for some reason."

"#### render-static-html

Use `notespace.api/render-static-html` to render the Notespace state of the notespace last touched as a standalone html file. This html should look exactly as the dynamic browser view at the moment of rendering."]

["### Editor integration

For a convenient interactive experience, it is ecommended to bind the Notespace API functions to functions of your editor.

You can see how to do this in Emacs in [this Elisp code](https://github.com/scicloj/notespace/blob/v3/emacs-config.el). In the future, we will probably provide similar solutions for other editors too."]

["## References

Clojure references such as delays, futures, promises and atoms are handled by Notespace in a special way. Below we will see how Notespace offers ways to bring the dynamic REPL experience of such constructs to the dynamic browser view.

### The rule of rendering

Assume that a note's evaluation result `x` is a reference. If `x` is already realized, then Notespace renders `@x` (dereferenced `x`) instead of `x`. Otherwise, you should see a mark that says it is still pending.

For example:"]

(def p1 (promise))
(def p2 (promise))
(deliver p2 14)
p1
p2

["### More API functions

Notespace offers some more API functions that allow for a dynamic experience with Clojure references."]

["#### eval-and-realize-note-at-line

Use `notespace.api/eval-and-realize-note-at-line` to evaluate the note at a certain line, realize the value if it is an unrealized derefable value, and inform Noteapace about the result.

For example, `(notespace.api/eval-and-realize-note-at-line 14)` applies that to the note at line 14."]

["### Futures"]

(require '[notespace.api :refer [F]])

["The `F` macro allows to define a Clojure future that informs Notespace when it is realized. This allows Notespace to update its state with the dereferenced value.

For example:"]

(def x
  (future
   (Thread/sleep 2000)
   (->> #(- (rand) 0.5)
        (repeatedly 999)
        (reductions +))))

["The following note should render after two seconds (of waiting for `x` to be realized)."]

(F (take 9 @x))

["### Delays"]

(require '[notespace.api :refer [D]])

["The `D` macro allows to define a Clojure delay that informs Notespace when it is realized. This allows Notespace to update its state with the dereferenced value.

For example:"]

(D (+ 1 2))

["If you called `notespace.api/eval-and-realize-note-at-line` with the line holding this note, then you should see the number 3 there. Otherwise, you should see a mark that says it is still pending."]

["### Atoms"]

(require '[notespace.api :refer [A]])

["The `A` function allows to define a Clojure atom that informs Notespace when it changes. This allows Notespace to update its state with the dereferenced value.

For example:"]

(def a
  (atom {:x 3}))

(A a)

["If you evaluated the code in the comment below once, then you should see `{:x 4}`. Otherwise, you should still see `{:x 3}`."]

^k/void
(comment
  (swap! a update :x inc))

["## Note kinds

Each note has a kind, that determines its rendering behaviour.

One way to specify a note's kind is by metadata. For example, here is a note of kind `:hiccup`."]

(require '[notespace.kinds :as k])

^k/hiccup
[:big [:big 3]]

["Let us see what kinds of notes we have.

### naive

A `:naive` note renders its value by pretty-printing it."]

^k/naive
(->> (partial rand-int 6)
     (partial repeatedly 6)
     (repeatedly 6))

["### void

A `:void` note does not render its value."]

^k/void
(+ 1 2)

["### hidden

A `:hidden` note does not render its value, and also does not show its code.

Here is a note that you cannot see:"]

^k/hidden
(+ 1 2)

["### md

Notes of kind `:md` are expected to return a seq of strings. These strings are concatenated and renderdered as Markdown."]

^k/md
["##### text:"
 "* **bold**
* *italic*
* [link](https://www.youtube.com/watch?v=oIw7E-q-flc)"]

^k/md
(cons
 "##### numbers:"
 (for [i (range 1 4)]
   (apply str "* " (repeat i i))))

["### md-nocode

Notes of kind `:md-nocode` are rendered as Markdown too, but without showing the code. Here is an example:"]

^k/md-nocode
(->> "abcd"
     (map (fn [ch]
            (str "* " ch))))

["### hiccup

A `:hiccup` note renders its value as Hiccup."]

^k/hiccup
[:big [:big 3]]

["### hiccup-nocode

Notes of kind `:hiccup-nocode` are rendered as Hiccup too, but without showing the code. Here is an example:"]

^k/hiccup-nocode
[:big [:big 3]]

["### extended hiccup

In fact, `:hiccup` and `:hiccup-nocode` render by an extended version of Hiccup, that supports [gorilla-ui](https://github.com/pink-gorilla/gorilla-ui) tags.

Here are some examples."]

^k/hiccup
[:div
 [:h5 "Plot:"]
 [:p/sparklinespot
  {:data      (->> #(- (rand) 0.5)
                   (repeatedly 99)
                   (reductions +))
   :svgHeight 20}]]

^k/hiccup
(into [:ul]
      (for [i (range 9)]
        [:li
         i " "
         [:p/sparklinespot
          {:data      (for [j (range 999)]
                        (+ (* 0.2 (rand))
                           (Math/sin (* i j))))
           :limit     100
           :svgWidth  100
           :svgHeight 20}]]))

^k/hiccup
[:p/player
 {:width  "100%"
  :height "100%"
  :url    "https://www.youtube.com/watch?v=G512fvK9KXA"}]

["Some additional tags such as `:p/code` are added by the [gorilla-notes](https://github.com/scicloj/gorilla-notes) infrastructure."]

^k/hiccup
[:p/code {:code "(defn abcd [x] (+ x 9))"}]

["## Other ways to specify note kinds.

A note's kinds can also be specified by including it in a vector beginning with a keyword. This is inspired by the approach of [Oz](https://github.com/metasoarous/oz), and should allow some partial compatibility with Oz notebooks."]

[:hiccup [:big [:big 3]]]

["Notes which are sequential forms beginning with a string (e.g., `[\"Hello!\" \"How are you?\"]`) are assigned the default kind `:md-nocode`  automatically."
 "This very text you are reading is an example of a note of that kind."]

["All other notes witn no explicit metadata are assigned the kind `:naive` automatically."]

(+ 1 2)

["## Interactive input and reactive notes

Coming soon."]

;; ^k/hiccup
;; [:p/slider :x {:min 0 :max 100 :initial-value 0}]

;; ^k/void
;; (require '[notespace.api :refer [R]])

;; (R [x]
;;    (* x 100))

["## Unit tests

Coming soon."]


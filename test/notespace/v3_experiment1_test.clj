(ns notespace.v3-experiment1-test
  (:require [notespace.api :as api]
            [clojure.java.browse :as browse]))

^k/hidden
(comment
  (api/init-with-browser)

  (api/init)

  (api/eval-this-notespace!)

  (api/render-static-html!)

  (browse/browse-url
   "doc/notespace/v3-experiment1-test/index.html"))

["# Notespace v3 intro

This is an experimantal incomplete draft. It should serve as a basis for discussion on our way to v3 of notespace."]

(require '[notespace.api :as api :refer [A D F]]
         '[notespace.kinds :as k])

["## Notes

Here is a note:"]

(+ 1 2
   3 4
   5 6)

["We see its code ant its return value rendered."]

["## Note kinds

Each note has a kind, that determines its rendering behaviour.

One way to specify a note's kind is by metadata. For example, here is a note of kind `:hiccup`."]

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
[:p/sparklinespot
 {:data      (->> #(- (rand) 0.5)
                  (repeatedly 9)
                  (reductions +))
  :svgHeight 20}]

["### extended hiccup

In fact, a `:hiccup` actually renders by an extended version of Hiccup. It supports where [gorilla-ui](https://github.com/pink-gorilla/gorilla-ui) tags.

Here are some examples."]

^k/hiccup
[:div
 [:h5 "Plot:"]
 [:p/sparklinespot
  {:data [5, 10, 5, 20, 10] :limit 5 :svgWidth 100 :svgHeight 20 :margin 5}]]

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

Another way is to assign a kind to a note is to include it in a vector beginning with a keyword. This is compatible with the approach of [Oz](https://github.com/metasoarous/oz)."]

[:hiccup [:big [:big 3]]]

["Most notes are assigned the default kind `:naive` automatically."]

(+ 1 2)

["Notes which are sequential forms beginning with a string (e.g., `[\"Hello!\" \"How are you?\"]`) are assigned the default kind `:md-nocode`  automatically."
 "This very text you are reading is an example of a note of that kind."]

["## References

Pending refs, such as delays, futures and promises, are rendered in a special way, taking into account whether they have been realized or not."]

^k/void
(def x
  (F
   (Thread/sleep 2000)
   (->> #(- (rand) 0.5)
        (repeatedly 999)
        (reductions +))))

(F (take 9 @x))

^k/hiccup
(F
 (let [y @x]
   (Thread/sleep 2000)
   [:p/sparklinespot
    {:data      y
     :svgHeight 20}]))

^k/hiccup
(D
 (Thread/sleep 1000)
 [:h3 (+ 1 2)])

(def a
  (atom {:x 3}))

(A a)

(comment
  (swap! a update :x inc))

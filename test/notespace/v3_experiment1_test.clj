(ns notespace.v3-experiment1-test
  (:require [notespace.api :refer [A D F]]
            [notespace.kinds :as k]))

["# Notespace v3 intro

This is an experimantal incomplete draft. It should serve as a basis for discussion on our way to v3."]

[:hi]

["Notes have kinds."]

["Most notes are assigned the default kind `:naive` automatically. This kind of note means rendering the value by pretty printing it:"]

(+ 1 2
   3 4
   5 6)

["Note kinds can be assigned explicitly through metadata."]

^k/naive
(->> (partial rand-int 6)
     (partial repeatedly 6)
     (repeatedly 6))

["Notes of kind `:void` do not render their values."]

^k/void
(+ 1 2)

["Notes of kind `:as-md` are expected to return a seq of strings. These strings are concatenated and renderdered as Markdown."]

^k/as-md
["##### text:"
 "* **bold**
* *italic*
* [link](https://www.youtube.com/watch?v=oIw7E-q-flc)"]

^k/as-md
(cons
 "##### numbers:"
 (for [i (range 1 4)]
  (apply str "* " (repeat i i))))

["Notes of kind `:md` are rendered as Markdown too, but without showing the code. Here is an example:"]

^k/as-md
(->> "abcd"
     (map (fn [ch]
            (str "* " ch))))

["Notes which are sequential forms beginning with a string (e.g., `[\"Hello!\" \"How are you?\"]`) are assigned the default kind `:md`  automatically."
 "This very text you are reading is an example of a note of that kind."]

["Notes of kind `:as-hiccup` are rendered as Hiccup. Actually it is an extended version of Hiccup, where [gorilla-ui](https://github.com/pink-gorilla/gorilla-ui) tags are allowed."]

^k/as-hiccup
[:div
 [:h5 "Plot:"]
 [:p/sparklinespot
  {:data [5, 10, 5, 20, 10] :limit 5 :svgWidth 100 :svgHeight 20 :margin 5}]]

^k/as-hiccup
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

^k/as-hiccup
[:p/player
 {:width  "100%"
  :height "100%"
  :url    "https://www.youtube.com/watch?v=G512fvK9KXA"}]

[" Some additional tags such as `:p/code` are added by the [gorilla-notes](https://github.com/scicloj/gorilla-notes) infrastructure."]

^k/as-hiccup
[:p/code {:code "(defn abcd [x] (+ x 9))"}]

["Notes of kind `:hiccup` are rendered as Hiccup too, but without showing the code. Here is an example:"]

^k/as-hiccup
[:p/sparklinespot
 {:data (->> #(- (rand) 0.5)
             (repeatedly 9)
             (reductions +))
  :svgHeight 20}]

["Pending refs, such as delays, futures and promises, are rendered in a special way, taking account whether they have been realized or not."]

^k/void
(def x
  (F
   (Thread/sleep 2000)
   (->> #(- (rand) 0.5)
        (repeatedly 999)
        (reductions +))))

(F (take 9 @x))

^k/as-hiccup
(F
 (let [y @x]
   (Thread/sleep 2000)
   [:p/sparklinespot
    {:data      y
     :svgHeight 20}]))

^k/as-hiccup
(D
 (Thread/sleep 1000)
 [:h3 (+ 1 2)])

(def a
  (atom {:x 3}))

(A a)

(comment
  (swap! a update :x inc))

[:bye]


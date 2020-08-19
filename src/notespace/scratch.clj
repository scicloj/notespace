(ns notespace.scratch
  (:require [notespace.api :as api]))

;; how to avoid shadow-cljs websockets on prod
;; clear newness notes
;; understand gn dirty state after some cljs errors

^{:hidden true}
(comment
  (api/init :open-browser? true)
  (api/init :open-browser? false)
  (api/reread-this-notespace!))

["Notes with no specified kind render by printing their return value."]

(+ 1 2
   3 4)

(repeat 9 (range 4))

["Notes of kind `:as-void` does not render their value."]

^{:void true}
(+ 1 2)

["Notes of kind `:as-hiccup` render as Hiccup.
Note that `gorilla-ui` tags are allowed here, as well as some additional tags such as `:p/code`."]

^{:as-hiccup true}
[:p/code {:code "(defn abcd [x] (+ x 9))"}]

^{:as-hiccup true}
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

["Notes of kind `:as-md` are expected to return a seq of strings. These strings are concatenated and renderdered as Markdown."]

^{:as-md true}
(concat ["#### numbers:"]
        (for [i (range 4)]
          (str "* " i)))

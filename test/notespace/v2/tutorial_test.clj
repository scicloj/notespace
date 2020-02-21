(ns notespace.v2.tutorial-test
  (:require [notespace.v2.note :as note
             :refer [note note-void
                     note-md note-hiccup
                     note-as-md note-as-hiccup
                     check]]
            [notespace.v2.live-reload]))

(note-md :Intro "## Intro")

(note-md "This is a notespace -- a namespace that is a sequence of notes. It is intented to be a tutorial about creating and using notespaces.")

(note-md "It is recommended to read this notespace alongside its rendered result.")

(note-md :Computing-and-rendering "## Computing and rendering")

(note-md "To use notespace, require the namespace `notespace.v2.note`, bringing the symbols of the relevant note kinds to your own namespace using `:refer`. For example, if you want to use notes of kind `:md`, you may like to `(require '[notespace.v2.note :refer [note-md]])`. More about note kinds -- below.")

(note-md "To see your rendered results with a live-reload experience, require the namespace `notespace.v2.live-reload` too. This will setup a live-reload server using Ring and Mirador, and will open a browser showing the rendered data.")

(note-md "To compute and render the whole notespace, call:
```
(notespace.v2.note/compute-this-notespace!)
```")

(note-md "To compute and rerender a certain note, call:
```
(notespace.v2.note/compute-note-at-line! line)
```
where `line` is one of the lines of the file that are taken by that note.")

(note-md "The notespace library is still experimental. **In the (not unlikely) that something breaks**, our current recommendation to reload the main notespace namespace, so that the broken state will overridden.
```
(require 'notespace.v2.note :reload)
```
 Soon things will be more robust.

Sometimes, the live-reload stops happening. A browser refresh sometimes fixes that.")

(note-md :Editor-integration "## Editor integration.")

(note-md "It is handy to be able to invoke the API functions using commands (and keybindings) of your editor. All you need is to figure out how to make your editor call Clojure functions and pass them the current line of the cursor.")

(note-md "In Emacs, one may do this using the following Elisp code:")

(note-md
 "```
 (defun notespace/compute-note-at-line ()
  (interactive)
  (save-buffer)
  (cider-interactive-eval
   (concat \"(notespace.v2.note/compute-note-at-line! \"
           (number-to-string (count-lines 1 (point)))
           \")\")
   (cider-interactive-eval-handler nil (point))
   nil
   nil)))

(defun notespace/compute-this-notespace ()
  (interactive)
  (save-buffer)
  (cider-interactive-eval
   \"(notespace.v2.note/compute-this-notespace!)\"
   (cider-interactive-eval-handler nil (point))
   nil
   nil)
  ```
  ")

(note-md :Note-kinds "## Note kinds")

(note-md "We can have all kinds of different notes. The different kinds behave differently when rendered.")

(note-md "This one is a note of kind `:md`. When rendered, it renders as markdown and does **not** show the source string.")

(note-md "The following is a 'regular' note, of kind `:code`. When renders, it shows both the source code and the printed return value.")

(note (def x 1)
      (+ x 1))

(note-md "The following is a note of kind `:void`. It shows the source, but does not show the output.")

(note-void (+ 1 2))

(note-md "The following is a note of kind `:hiccup`. It does not show the source, and renders the return value as hiccup.")

(note-hiccup
 (->> 4
      range
      (map (fn [i] [:li i]))
      (into [:ul])))

(note-md "Sometimes, we may want to render things as markdown or hiccup, but do want to see the source too. The following two kinds of notes are useful in those cases.")

(note-md "This is a note of kind `:as-md`. Shows the source, renders as markdown.")

(note-md "The following is a note of kind `:as-hiccup`. Shows the source, renders as hiccup.")

(note-as-hiccup
 (->> 4
      range
      (map (fn [i] [:li i]))
      (into [:ul])))

(note-md (markdown.core/md-to-html-string
          "Here is the summary of the note kinds we mentioned:
(Sorry for this strange table. Markdown tables do not render properly at the moment.)

 **| kind       | symbol        | render source? | value-rendering |**

 | code      | note          | v              | pretty printing |

 | void      | note-void     | v              | x               |

 | md        | note-md       | x              | as markdown     |

 | as-md     | note-as-md    | v              | as markdown     |

 | hiccup    | note-hiccup   | x              | as hiccup       |

 | as-hiccup | note-as-hiccu | v              | as hiccup       |
"))

(note-md :Labels "## Labels")

(note-md "In the definition of the following note, we begin with a keyword: `:label-example`. This keyword is called the label of the note, and it is not rendered. It creates a link to that note from the table-of-contents.")

(note :label-example
      (+ 1 2))

(note-md :Tables "## Tables")

(note-md "It may be handy to render data in interactive tables. We have some auxiliary functions to render this using the [DataTables](https://datatables.net) JS library.")

(note-as-hiccup
 (require '[notespace.v2.table :as table])
 (table/->datatable
  (for [i (range 99)]
    {:x i
     :y (if (even? i) "a" "b")
     :z (rand)})))

(note-md :Graphics "## Graphics")

(note-md "Graphics can be rendered as hiccup. Here is an example using SVG.")

(note-as-hiccup
 [:svg {:height 100 :width 100}
  [:circle {:cx 50 :cy 50 :r 40
            :stroke "purple" :stroke-width 4
            :fill "lightgrey"}]])

(note-md :Tests "## Tests")

(note-md "One may use the `check` function to create tests using arbitrar functions.")

(note (->> (+ 1 2)
           (check = 3)))

(note (check pos? -9))

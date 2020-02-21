(ns notespace.v1.examples.example
  (:require [notespace.v1.note :as note
             :refer [note note-md]]
            [clojure.pprint :as pp]
            [hiccup.core :as hiccup
             :refer [html]]
            [hiccup.page :as page]))

(note-md
 "#Title
##Subtitle
text
")

(note-md "Define a function:")

(note
 (defn f [x]
   (+ x 3)))

(note-md "Use the function:")

(note
 (delay
   [:hiccup
    [:h5 (-> 12 f)]]))

(note-md "Create some data:")

(note  (def n 14)
       (for [i (range n)] {:a i}))


(note/render-ns! *ns*)



(ns notespace.examples.example
  (:require [notespace.note :as note
             :refer [note note-md]]
            [clojure.pprint :as pp]
            [hiccup.core :as hiccup
             :refer [html]]
            [hiccup.page :as page]
            [clojuress :as r :refer [r]]))

(note-md
 "#Title
##Subtitle
")

(note-md "Define a function:")

(note
 (defn f [x]))


(note-md "Use the function:")

(note
 (delay
   [:hiccup
    [:h5 (-> 4 f)]]))

(note-md "Create some data:")

(note  (def n 4)
       (for [i (range n)] {:a i}))

(note/render-ns! *ns*)
(note/render-ns! *ns*)


(ns notespace.examples.example
  (:require [notespace.note :as note
             :refer [note]]
            [clojure.pprint :as pp]
            [hiccup.core :as hiccup
             :refer [html]]
            [hiccup.page :as page]
            [clojuress :as r :refer [r]]))



(note
 (defn f [x]
   (+ x 11)))

(note
 (delay
   [:hiccup
    [:h5 (-> 4 f)]]))

(note  (def n 3)
       (for [i (range n)] {:a i}))

(note/render-ns! *ns*)
(note/render-ns! *ns*)



(ns notespace.examples.example
  (:require [notespace.note :as note
             :refer [note]]
            [clojure.pprint :as pp]
            [hiccup.core :as hiccup
             :refer [html]]
            [hiccup.page :as page]
            #_[clojuress :as r :refer [r]]))


(comment
 (note/update-notes! *ns*)

 @note/ns->forms->note-idx

 @note/ns->notes

 (note/ns-expressions *ns*)

 (note/ns-notes *ns*)

 (note/updated-notes *ns*)
)



(note
 (defn f [x]
   (do
     (+ x 11))))

(note
 (delay
   [:hiccup
    [:h5 (-> 109 f)]]))

(note  (def n 11)
       (for [i (range n)] {:a i}))

(note/render-ns! *ns*)
(note/render-ns! *ns*)


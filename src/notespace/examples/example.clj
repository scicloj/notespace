(ns notespace.examples.example
  (:require [notespace.note :as note
             :refer [note]]
            [clojure.pprint :as pp]
            [hiccup.core :as hiccup
             :refer [html]]
            [hiccup.page :as page]
            #_[clojuress :as r :refer [r]]))



(note :kind1
 (defn f [x]
   (do
     (println [:f x])
     (+ x 2233222222311)))
 :ok1)


(note :kind1
 (delay
   (html [:h3 (-> 22211 f)])))


(note/render-ns! *ns*)


(comment



  @note/ns->notes

    )

(ns notespace.v0.cdn
  (:require [hiccup.page :as page]
            [hiccup.core :as hiccup]))


(def urls
  {:prettify   [[:js "https://cdn.jsdelivr.net/gh/google/code-prettify@master/loader/run_prettify.js"]
                [:js "https://cdnjs.cloudflare.com/ajax/libs/prettify/r298/lang-clj.js"]]
   :datatables [[:css "https://cdn.datatables.net/1.10.20/css/jquery.dataTables.min.css"]
                [:js "https://code.jquery.com/jquery-3.4.1.min.js"]
                [:js "https://cdn.datatables.net/1.10.20/js/jquery.dataTables.min.js"]]})

(def include-fns
  {:css page/include-css
   :js  page/include-js})

(defn header [dep]
  (->> dep
       urls
       (map (fn [[tag-type url]]
              ((include-fns tag-type) url)))
       (apply concat)))


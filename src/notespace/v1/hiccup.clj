(ns notespace.v1.hiccup
  (:require [clojure.string :as string]
            [zprint.core :as zprint]
            [clojure.pprint :as pp]
            [hiccup.core :as hiccup]
            [markdown.core :refer [md-to-html-string]]))

(defn htmlify-whitespace [s]
  (-> s
      (string/replace #"\n" "</br>")
      (string/replace #" " "&nbsp;")))

(defn code->hiccup [code]
  [:code {:class "prettyprint lang-clj"}
   (-> code
       (string/replace #"\n" " ")
       (string/replace #"^[(]([a-z-]*)" "")
       (string/replace #"[)]$" "")
       (zprint/zprint 40 {:parse-string? true})
       with-out-str
       htmlify-whitespace)])

(defn form->hiccup [form print-fn]
  [:code {:class "prettyprint lang-clj"}
   (-> form
       print-fn
       with-out-str
       htmlify-whitespace)])

(defn value->hiccup [v]
  (cond (fn? v)         ""
        (sequential? v) (case (first v)
                          :hiccup (hiccup/html v)
                          (form->hiccup v pp/pprint))
        :else           (form->hiccup v pp/pprint)))

(defn md->hiccup [md]
  [:div
   (md-to-html-string md)])

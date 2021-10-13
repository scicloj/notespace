(ns scicloj.notespace.v4.view
  (:require [clojure.string :as string]
            [gorilla-notes.core :as gn]
            [scicloj.notespace.v4.log :as v4.log]
            [scicloj.notespace.v4.note :as v4.note]))

(defn title [title]
  [:p [:small [:b title]]])

(defn messages->hiccup [messages]
  [:div
   (title "events log")
   (->> messages
        reverse
        (map (fn [message]
               [:small
                [:li message]]))
        (into [:ul {:style {:overflow-y "scroll"
                            :max-height "20px"}}]))])

(defn summary->hiccup [{:keys [current-path
                               current-notes
                               counts]
                        :as details}]
  [:small
   [:div
    [:p [:b "current path: "] current-path]
    [:p [:b "notes: "] (count current-notes)
     (when (seq counts)
       (str " " (pr-str counts)))]]])

(defn comment-source->hiccup [source]
  [:p/markdown
   (-> source
       (string/split #"\n")
       (->> (map #(string/replace % #"^\s*;*" ""))
            (string/join "\n")))])

(defn note->hiccup [[part {:keys [source gen status value comment?] :as note}]]
  (let [{:keys [render-src? value->hiccup]} (v4.note/behaviour note)]
    [:div
     (case part
       :view/source (when (and (not comment?)
                               render-src?)
                      [:p/code {:code     source
                                :bg-class "bg-light"}])
       :view/state  (if comment?
                      (comment-source->hiccup source)
                      ;; else
                      (when status
                        [:div
                         (case status
                           :evaluating "evaluating ..."
                           :failed     "failed"
                           :evaluated  (value->hiccup value))])))]))

(defn ->header [{:keys [messages last-evaluated-note]
                 :as   details}]
  [:div
   {:style {:background "#efefef"}}
   (messages->hiccup messages)
   (note->hiccup [:view/state last-evaluated-note])
   (summary->hiccup details)
   [:hr]])

(ns scicloj.notespace.v4.view
  (:require [clojure.string :as string]
            [gorilla-notes.core :as gn]
            [scicloj.notespace.v4.log :as v4.log]
            [scicloj.notespace.v4.note :as v4.note]
            [scicloj.notespace.v4.config :as v4.config]))

(defn summary->hiccup [{:keys [current-path
                               current-notes
                               counts]
                        :as details}]
  [:div
   [:p [:big [:big [:p/code (pr-str {:notespace current-path})]]]]
    [:p/code
     (->> counts
          (merge {:notes (count current-notes)})
          pr-str)]])

(defn comment-source->hiccup [source]
  [:div.container-fluid
   [:p/markdown
   (-> source
       (string/split #"\n")
       (->> (map #(string/replace % #"^\s*;*" ""))
            (string/join "\n")))]])

(defn note->hiccup [[part {:keys [source gen status value comment?] :as note}]]
  (let [{:keys [render-src? value->hiccup]} (v4.note/behaviour note)
        source-view (fn []
                      (if comment?
                        (comment-source->hiccup source)
                        ;; else
                        (when (and (not comment?)
                                   render-src?)
                          [:div.bg-light.pt-4.pb-2
                           [:div.container-fluid [:p/code {:code source}]]])))
        state-view (fn []
                     (if comment?
                       ""
                       (if status
                         [:div #_{:style {:background "floralwhite"}}
                          [:div.container-fluid
                           (case status
                             :evaluating "evaluating ..."
                             :failed     "failed"
                             :evaluated  (value->hiccup value))]]
                         [:div.mb-3])))
        both-view (fn []
                    [:div
                     [:div {:style {:display :inline-block
                                    :vertical-align :top
                                    :width "50%"}}
                      (source-view)]
                     [:div {:style {:display :inline-block
                                    :vertical-align :top
                                    :width "50%"}}
                      (state-view)]
                     [:br]
                     [:br]])]
    [:div
     (case part
       :view/source (source-view)
       :view/state (state-view)
       :view/both (both-view))]))

(defn ->header [{:keys [current-path]
                 :as   details}]
  [:div
   [:br]
   [:p
    {:style {:margin      "0 10px"
             ;; :font-family "'Fira Code'"
             }}
    current-path]]
  ;; (let [{:keys [messages? summary?]} @v4.config/*config]
  ;;   [:div.bg-light
  ;;    [:p ""]
  ;;    (when summary?
  ;;      (summary->hiccup details))])
  )


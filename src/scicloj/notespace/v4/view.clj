(ns scicloj.notespace.v4.view
  (:require [clojure.string :as string]
            [gorilla-notes.core :as gn]
            [scicloj.notespace.v4.log :as v4.log]
            [scicloj.notespace.v4.note :as v4.note]
            [scicloj.notespace.v4.kinds :as v4.kinds]))

(def lightgreyback {:style {:background "#efefef"}})

(defn title [title]
  [:p [:small [:b title]]])

(defn messages->hiccup [messages]
  [:div
   (->> messages
        reverse
        (map (fn [message]
               [:small
                [:li message]]))
        (into [:ul {:style {:overflow-y "scroll"
                            :max-height "20px"}}]))])

(defn last-value->hiccup [last-value]
  [:div
   (let [{:keys [value->hiccup]} (v4.note/value->behavior last-value)]
     (value->hiccup last-value))])

(defn summary->hiccup [{:keys [current-path
                               current-notes
                               counts]
                        :as details}]
  [:div
   [:p [:b "path: "] current-path]
   [:p [:b "notes: "] (count current-notes)
    (when (seq counts)
      (str " " (pr-str counts)))]])

(defn comment-source->hiccup [source]
  [:p/markdown
   (-> source
       (string/split #"\n")
       (->> (map #(string/replace % #"^\s*;*" ""))
            (string/join "\n")))])

(defn note->hiccup [{:keys [source gen status comment?] :as note}]
  (let [{:keys [render-src? value->hiccup]} (v4.note/behavior note)]
    (if comment?
      (comment-source->hiccup source)
      ;; else
      [:div
       ;; (pr-str (v4.note/kind note))
       ;; (pr-str (v4.note/behavior note))
       (when render-src?
         [:div lightgreyback [:p/code source]])
       [:p
        (when-let [{:keys [state value]} status]
          (case state
            :evaluating "evaluating ..."
            :failed "failed"
            :evaluated (value->hiccup value)))]])))

(defn notes->hiccup [notes]
  (->> notes
       (map note->hiccup)
       (into [:div])))

(defn ->header [{:keys [messages last-value]
                 :as details}]
  [:div
   (title "events log")
   (messages->hiccup messages)
   [:hr]
   (title "last value")
   (last-value->hiccup last-value)
   [:hr]
   (title "summary")
   (summary->hiccup details)
   [:hr]
   (title "notes")])

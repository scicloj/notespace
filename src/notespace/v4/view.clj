(ns notespace.v4.view
  (:require [clojure.string :as string]
            [gorilla-notes.core :as gn]
            [notespace.v4.render :as v4.render]))

(def lightgreyback {:style {:background "#efefef"}})

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
                            :max-height "60px"}}]))
   [:hr]])

(defn comment-source->hiccup [source]
  [:p/markdown
   (-> source
       (string/split #"\n")
       (->> (map #(string/replace % #"^\s*;*" ""))
            (string/join "\n")))])


(defn note->hiccup [{:keys [source gen status comment?] :as note}]
  (if comment?
    (comment-source->hiccup source)
    ;; else
    [:div
     #_[:small lightgreyback "gen" gen]
     [:div lightgreyback [:p/code source]]
     [:p
      (when-let [{:keys [state]} status]
        (case state
          :evaluating "evaluating ..."
          :evaluated  (-> status
                          :value
                          v4.render/render)))]]))

(defn notes->hiccup [notes]
  (->> notes
       (map note->hiccup)
       (into [:div])))



(defn update-view [last-value notes]
  (->> [:div
        (title "last value")
        (or (some-> last-value v4.render/render)
            "-")
        [:hr]
        (title "notes")
        (notes->hiccup notes)]
       (gn/assoc-note! 0)))

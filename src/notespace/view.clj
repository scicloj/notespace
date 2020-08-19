(ns notespace.view
  (:require [notespace.context :as ctx]
            [markdown.core :as md]
            [clojure.string :as string]
            [clojure.pprint :as pp]))



(defn note->hiccup [note value]
  (when-let [{:keys [render-src? value->hiccup]} (ctx/sub-get-in
                                                  :kind->behaviour
                                                  (:kind note))]
    [:div
     (when render-src?
       [:p/code {:code (-> note :metadata :source)
                 :bg-class "bg-light"}])
     [:p (value->hiccup value)]]))

(defn value->naive-hiccup [value]
  [:p/code {:code (-> value
                      pp/pprint
                      with-out-str)}])

(defn markdowns->hiccup [mds]
  [:p/markdown (string/join "\n" mds)])


;; (defn code->hiccup [code & {:keys [remove-label?]}]
;;   [:code {:class "prettyprint lang-clj"}
;;    (-> code
;;        (zprint/zprint 72 {:parse-string? true})
;;        with-out-str
;;        htmlify-whitespace)])

;; (defn form->hiccup [form print-fn]
;;   [:code {:class "prettyprint lang-clj"}
;;    (-> form
;;        print-fn
;;        with-out-str
;;        (string/split #"\n")
;;        (->> (filter (complement (partial re-find #"nRepl-session")))
;;             (string/join "\n"))
;;        htmlify-whitespace)])

;; (defn value->hiccup [v]
;;   [:div {:class "nspout"}
;;    (cond (fn? v)         ""
;;          (sequential? v) (case (first v)
;;                            :hiccup (hiccup/html v)
;;                            (form->hiccup v pp/pprint))
;;          :else           (form->hiccup v pp/pprint))])

;; (defn label->anchor-id [label]
;;   (->> label name))

;; (defn label->anchor [label]
;;   [:a  {;; :style "border: 2px solid green;"
;;         :id (label->anchor-id label)}
;;    " "])


;; ;; We can render the notes of a namespace to the file.
;; (defn note-and-state->hiccup [{:keys [forms label kind]
;;                                :as   anote}
;;                               {:keys [value rendered]
;;                                :as   note-state}]
;;   [:div {:class "nspbox"}
;;    (when label
;;      (label->anchor label))
;;    (when (ctx/sub-get-in :kind->behaviour kind :render-src?)
;;      (->> (or [(some-> anote
;;                        :metadata
;;                        :source
;;                        (code->hiccup
;;                         :remove-label? label))]
;;               (->> (if label
;;                      (rest forms)
;;                      forms)
;;                    (map (fn [form]
;;                           [:div
;;                            (-> form
;;                                (form->hiccup #(careful-zprint % 80)))]))))
;;           (into [:div {:class "nspin"}])))
;;    (if (fresh? value)
;;      rendered
;;      [:div
;;       "--"
;;       [:img {:src "./waiting.gif"}]])])

;; (defn ->reference [namespace]
;;   [:div
;;    [:i
;;     [:small
;;      (if-let [url (repo/ns-url namespace)]
;;        [:a {:href url} namespace]
;;        namespace)
;;      " - created by " [:a {:href "https://github.com/scicloj/notespace"}
;;                        "notespace"] ", " (java.util.Date.) "."]]
;;    [:hr]])


;; (defn toc [notes]
;;   (when-let [labels (->> notes
;;                          (map :label)
;;                          (filter some?)
;;                          seq)]
;;     [:div
;;      "Table of contents"
;;      (->> labels
;;           (map (fn [label]
;;                  [:li [:a {:href (->> label
;;                                       label->anchor-id
;;                                       (str "#"))}
;;                        (name label)]]))
;;           (into [:ul]))
;;      [:hr]]))

;; (defn notes-and-states->hiccup [namespace notes note-states]
;;   (let [checks-freqs   (check/->checks-freqs notes)
;;         checks-summary (check/->checks-summary checks-freqs)
;;         reference      (->reference namespace)]
;;     (when checks-freqs
;;       (log/info [::checks checks-freqs]))
;;     [:div
;;      [:h1 (str namespace)]
;;      reference
;;      checks-summary
;;      (toc notes)
;;      (map note-and-state->hiccup
;;           notes
;;           note-states)
;;      [:hr]
;;      checks-summary
;;      reference]))

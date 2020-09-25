(ns notespace.view
  (:require [clojure.string :as string]
            [clojure.pprint :as pp]
            [notespace.util :as u]
            [notespace.state :as state]))


(def waiting
  [:div
   [:big [:big "..."]]])

(defn note->hiccup [{:keys [value metadata kind]}]
  (when-let [{:keys [render-src? value->hiccup]}
             (state/sub-get-in
              :kind->behaviour
              kind)]
    [:div
     (when render-src?
       [:p/code {:code     (:source metadata)
                 :bg-class "bg-light"}])
     ;; TODO Simplify the logic here.
     [:div (if (u/ready? value)
             (if (var? value)
               (-> value
                   value->hiccup)
               (-> value
                   u/realize
                   value->hiccup))
             waiting)]]))

(defn value->naive-hiccup [value]
  [:p/code {:code (-> value
                      pp/pprint
                      with-out-str)}])

(defn markdowns->hiccup [mds]
  (if-not (sequential? mds)
    (markdowns->hiccup [mds])
    [:p/markdown
     (->> mds
          (map #(-> % print with-out-str))
          (string/join "\n"))]))

(defn safe-value [x]
  (if (instance? java.time.LocalDateTime x)
    (.toInstant ^java.time.LocalDateTime x)
    x))

(defn dataset->grid-hiccup [ds]
  (let [max-n-rows          100
        string-column-names (->> ds
                                 keys
                                 (map name))
        column-defs         (->> string-column-names
                                 (mapv (fn [k-str]
                                         {:headerName k-str
                                          :field      k-str})))
        columns             (->> ds
                                 vals
                                 (map (partial take max-n-rows)))
        row-data            (apply
                             map
                             (fn [& row-values]
                               (->> row-values
                                    (map safe-value)
                                    (zipmap string-column-names)))
                             columns)]
    [:div {:class "ag-theme-balham"
           :style {:height "150px"}}
     [:p (format "(showing first %d rows)" max-n-rows)]
     [:p/dataset {:columnDefs column-defs
                  :rowData    row-data}]]))

(defn dataset->md-hiccup [mds]
  [:div {:class "table table-striped table-hover table-condensed table-responsive"
         :style {:height "400px"}}
   (markdowns->hiccup mds)])

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

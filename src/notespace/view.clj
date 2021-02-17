(ns notespace.view
  (:require [clojure.string :as string]
            [clojure.pprint :as pp]
            [notespace.util :as u]
            [notespace.state :as state]
            [notespace.repo :as repo]
            [notespace.util :as util]
            [cljfx.api :as fx]
            [notespace.context :as ctx]))

(def notespace-style
  {:style {:font-style  "italic"
           :font-family "\"Lucida Console\", Courier, monospace"}})

(defn status-description [text]
  [:p notespace-style
   [:small (format "(%s)" text)]])

(defn details->hiccup [value source kind]
  (let [actual-kind (-> value
                        meta
                        :notespace.kind
                        (or kind))]
    (println (pr-str [:actual-kind
                      value
                      actual-kind]))
    (when-let [{:keys [render-src? value->hiccup]}
               ((state/sub-get-in :kind->behaviour) actual-kind)]
      [:div
       (when (and  render-src? (state/sub-get-in :config :render-src?))
         [:p/code {:code     source
                   :bg-class "bg-light"}])
       (value->hiccup value)])))

(defn note->hiccup [{:keys [value metadata kind stage]}]
  (let [->hiccup (fn [v]
                   (details->hiccup v (:source metadata) kind))]
    (cond
      ;;
      (= value :notespace.note/failed)
      (status-description "failed")
      ;;
      (u/ready? value)
      (cond ;;
        (var? value)
        (->hiccup value)
        ;;
        (instance? clojure.lang.IDeref value)
        [:div
         (status-description "dereferenced")
         (->hiccup @value)]
        ;;
        :else
        (->hiccup value))
      ;; else
      :else
      (status-description
       (cond ;;
         (= stage :initial)
         "not evaluated yet"
         ;;
         (delay? value)
         (if (= stage :realizing)
           "delay - already running ..."
           "delay - not running yet ...")
         ;;
         (future? value)
         "future - running ..."
         ;;
         :else
         "not ready - unknown reason")))))

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

  (let [ds (if (sequential? ds)
             (util/map-coll->key-vector-map ds)
             ds)
        max-n-rows          100
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
     (status-description
      (format "showing at most %d rows" max-n-rows))
     [:p/dataset {:columnDefs column-defs
                  :rowData    row-data}]]))

(defn dataset->md-hiccup [mds]
  (let [height (* 46 (- (count (string/split-lines (str mds))) 2))
        height-limit (min height 400)]
    [:div {:class "table table-striped table-hover table-condensed table-responsive"
           :style {:height (str height-limit "px")}}
     (markdowns->hiccup mds)]))

(defn bool->symbol [bool]
  (if bool
    [:big [:big {:style {:color "darkgreen"}}
           "✓"]]
    [:big [:big {:style {:color "darkred"}}
           "❌"]]))

(defn test-boolean->hiccup [bool]
  [:div
   (bool->symbol bool)
   (str "   " bool)])

(defn ->reference [namespace]
  [:div
   [:i
    [:small
     (if-let [url (repo/ns-url namespace)]
       [:a {:href url} namespace]
       namespace)
     " - created by " [:a {:href "https://github.com/scicloj/notespace"}
                       "notespace"] ", " (str (java.util.Date.)) "."]]
   [:hr]])


(defn label->anchor-id [label]
  (->> label name))

(defn label->anchor [label]
  [:a  {;; :style "border: 2px solid green;"
        :id (label->anchor-id label)}
   " "])


(defn toc [notes]
  (when-let [labels (->> notes
                         (map :label)
                         (filter some?)
                         seq)]
    [:div
     "Table of contents"
     (->> labels
          (map (fn [label]
                 [:li [:a {:href (->> label
                                      label->anchor-id
                                      (str "#"))}
                       (name label)]]))
          (into [:ul]))
     [:hr]]))

(defn notes-count [notes]
  [:p (count notes) " notes"])

(defn notes->midje-summary [notes]
  [:div
   (some->> notes
           (filter #(= (:kind %) :notespace.kinds/midje))
           (map :value)
           frequencies
           seq
           (sort-by key)
           (map (fn [[bool freq]]
                  [:div
                   (bool->symbol bool)
                   ": "
                   freq]))
           (into [:div
                  "Tests summary:"]))])

(defn notes->header-and-footer [namespace notes]
  (let [reference     (->reference namespace)
        midje-summary (notes->midje-summary notes)]
    {:header [:div notespace-style
              "(notespace)"
              [:p (str (java.util.Date.))]
              ;; (some-> notes notes-count)
              ;; reference
              (some-> notes toc)
              [:hr]]
     :footer [:div
              [:hr]
              [:hr]]}))

(defn header-and-footer [namespace]
  (->> namespace
       (state/sub-get-in :ns->notes)
       (notes->header-and-footer namespace)))

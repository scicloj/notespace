(ns notespace.v0.core
  (:require [clojure.string :as string]
            [hiccup.core :as hiccup]
            [hiccup.page :as page]
            [notespace.v0.util :refer [pprint-and-return]]
            [clojure.pprint :as pp]
            #_[zprint.core :as zp]
            [cljfmt.core]
            [rewrite-clj.node]))


(defonce ns->notes (atom {}))

(defrecord Note [forms change transitive-change fresh value])

(defn ns-expressions [namespace]
  (-> namespace
      str
      (string/replace #"\." "/")
      (->> (format "src/%s.clj")
           slurp
           (format "[%s]")
           read-string)))

(defn expr->Note
  ([expr]
   (when (and (sequential? expr)
              (-> expr first (= 'note)))
     (let [[kind & forms] (rest expr)]
       (->Note (-> kind
                   name
                   keyword)
               (vec forms)
               false
               false
               false
               nil)))))

(defn ns-notes [namespace]
  (->> namespace
       ns-expressions
       (map expr->Note)
       (filter some?)))

(def kind->behaviour
  {:fn  {:eval          true
         :naturally-fresh true
         :rendered      false}
   :out {:eval          false
         :naturally-fresh false
         :rendered      true}})

(defn update-note-change [prev-note note]
  (assoc note
         :change (not= (:forms prev-note)
                       (:forms note))))

(defn update-note-fresh [note]
  (assoc note
         :fresh (or (-> note :kind kind->behaviour :naturally-fresh)
                    (and (:fresh prev-note)
                         (not changed)))))

(defn update-notes-transitive-change [notes]
  (map (fn [note transitive-change]
         (assoc note :transitive-change transitive-change))
       notes
       (->> notes
            (map :changed)
            (reductions #(or %1 %2)))))

(defn update-notes [prev-notes notes]
  (let [notes1 (map updated-note
                    (concat prev-notes
                            (repeat nil))
                    notes)
        sticky-changes (->> notes1
                            (map :changed)
                            (reductions #(or %1 %2)))]
    (->> notes1
         (map (fn [sticky-change note]
                (assoc note :changed sticky-change))
              sticky-changes)
         (map (fn [note]
                (assoc note
                       :fresh (or (-> note :kind kind->behaviour :naturally-fresh)
                                  (not (:changed note)))))))))

(defn update-ns-notes! []
  (swap! ns->notes
         update
         *ns*
         #(update-notes
           %
           (ns-notes *ns*))))

(defn refresh-note! [anote]
  (println [:refreshing anote])
  (if (-> anote :fresh not)
    (do
      (println "__________________")
      (assoc anote
           :fresh true
           :value (if  (-> anote :kind kind->behaviour :rendered)
                    (->> anote
                         :forms
                         (map eval))
                    (doseq [form (:forms anote)]
                      (eval form)))))
    anote))

(defn refresh-ns! []
  (update-ns-notes!)
  (-> ns->notes
      (swap! update *ns*
             (partial mapv refresh-note!))
      (get *ns*)))

(defn rerender! []
  (->> (refresh-ns!)
       pprint-and-return
       (map (fn [anote]
              (into [:div
                     (println anote)
                     (->> anote
                          :forms
                          (map (fn [form]
                                 [:p
                                  [:code {:class "prettyprint"}
                                   (->> form
                                        pp/pprint
                                        with-out-str
                                        cljfmt.core/reformat-string)]]))
                          (into [:div
                                 {:style "background-color:#eeeeee;"}]))
                     [:p (if (:fresh anote)
                           ""
                           "[?]")]]
                    (:value anote))))
       (into [:div])
       (vector :body
               {:style "background-color:#cccccc; align:center"})
       (vector :head
               (page/include-js
                "https://cdn.jsdelivr.net/gh/google/code-prettify@master/loader/run_prettify.js"))
       hiccup/html
       page/html5
       (spit "resources/public/index.html")))

(defmacro note [kind & forms]
  (when (-> kind keyword kind->behaviour :eval)
    (cons 'do forms)))


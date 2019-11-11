(ns notespace.note
  (:require [clojure.string :as string]
            [hiccup.core :as hiccup]
            [hiccup.page :as page]
            [notespace.util :refer [fmap only-one pprint-and-return]]
            [clojure.pprint :as pp]
            [cljfmt.core]
            [rewrite-clj.node]
            [clojure.java.io :as io]
            [clojure.java.browse :refer [browse-url]]
            [zprint.core :as zprint]
            [clojure.java.shell :refer [sh]])
  (:import java.io.File
           clojure.lang.IDeref))

;; A note has a kind, a collection of forms, a return value, a rendered result, and a status.
(defrecord Note [kind forms value rendered status])

;; We have a catalogue of notes -- a sequence of notes per namespace.
(def ns->notes (atom {}))

;; We can also find a note's location in the sequence by its forms.
;; Here we assume that no two notes have the same forms.
(def ns->forms->note-idx (atom {}))

;; We also keep track of changes in source files corresponding to namespaces.
(def ns->last-modification (atom {}))

(defn ns->src-filename [namespace]
  (-> namespace
      str
      (string/replace "." "/")
      (->> (format "src/%s.clj"))))

(defn src-file-modified? [namespace]
  (let [previously-last-modified  (@ns->last-modification namespace)
        last-modified (-> *ns* ns->src-filename io/file (.lastModified))]
    (swap! ns->last-modification assoc namespace last-modified)
    (not= previously-last-modified last-modified)))

;; We can collect all expressions in a namespace.
(defn ns-expressions [namespace]
  (-> namespace
      str
      (string/replace #"\." "/")
      (->> (format "src/%s.clj")
           slurp
           (format "[%s]")
           read-string)))

;; Each expression of the form '(note ...) can be converted to a note.
(defn expr->Note
  ([expr]
   (when (and (sequential? expr)
              (-> expr first (= 'note)))
     (let [[kind & forms] (rest expr)]
       (->Note (-> kind
                   name
                   keyword)
               (vec forms)
               nil
               nil
               {})))))

;; Thus we can collect all notes in a namespace.
(defn ns-notes [namespace]
  (->> namespace
       ns-expressions
       (map expr->Note)
       (filter some?)))

;; We can get the updated notes of a namespace.
;; We try not to update things that has not changed.
(defn updated-notes [namespace]
  (let [old-notes (@ns->notes namespace)
        modified (src-file-modified? namespace)]
    {:modified modified
     :notes (if (not modified)
              old-notes
              (let [new-notes (ns-notes *ns*)]
                (mapv (fn [old-note new-note]
                       (let [change (not= (:forms old-note)
                                          (:forms new-note))]
                         (if change
                           (assoc new-note :status :changed)
                           old-note)))
                     (concat old-notes (repeat nil))
                     new-notes)))}))

;; We can update our memory regarding the notes in the namespace.
(defn update-notes! [namespace]
  (let [{:keys [modified notes]} (updated-notes namespace)]
    (when modified
      (swap! ns->notes assoc namespace notes)
      (swap! ns->forms->note-idx assoc namespace
             (->> notes
                  (map-indexed (fn [idx note]
                                 {:idx  idx
                                  :note note}))
                  (group-by (comp :forms :note))
                  (fmap (comp :idx only-one)))))
    notes))

;; Given the forms of a note in a namespace,
;; we can check its location in the sequence of notes.
(defn location [namespace forms]
  (get-in @ns->forms->note-idx [namespace forms]))

;; When a note is evaluated,
;; itw forms are evaluated, and the catalogue of notes is updated.
(defmacro note [kind & forms]
  (update-notes! *ns*)
  (let [value (eval (cons 'do forms))
        idx (location *ns* forms)]
    (swap! ns->notes assoc-in [*ns* idx :value]
           value)
    `(get-in @ns->notes [~*ns* ~idx])))

;; A note is rendered in the following way:
;; If its value is an IDeref, then the it is dereffed.
;; Otherwise, its value is taken as-is.
;; The rendered value is saved.
(defn render! [anote]
  (let [v (:value anote)
        rendered (if (instance? IDeref v)
                   @v
                   v)
        idx (->> anote :forms (location *ns*))]
    (swap! ns->notes update-in [*ns* idx]
           #(merge %
                   {:rendered rendered
                    :status   :fresh}))
    rendered))

;; Any namespace has a corresponding output html file.
(defn ns->out-filename [namespace]
  (let [filename  (-> namespace
                      str
                      (string/replace "." "/")
                      (->> (format "resources/public/%s/index.html")))
        dir (-> filename
                (File.)
                (.getParentFile))]
    (when-not (.exists dir)
      (.mkdirs dir))
    filename))

;; We can render the notes of a namespace to the file.
(defn render-ns! [namespace]
  (let [notes (@ns->notes namespace)]
    (doseq [anote notes]
      (render! anote))
    (->> notes
         (map (fn [anote]
                (->> anote
                     :forms
                     (map (fn [form]
                            [:div
                             [:p
                              [:code {:class "prettyprint"}
                               #_(->> form
                                    zprint/zprint-str)
                               (->> form
                                    pp/pprint
                                    with-out-str
                                    cljfmt.core/reformat-string)]]]))
                     (#(concat % [(:rendered anote)]))
                     (into [:div
                            {:style "background-color:#eeeeee;"}]))))
         (into [:div])
         (vector :body
                 {:style "background-color:#cccccc;"})
         (vector :head
                 (page/include-js
                  "https://cdn.jsdelivr.net/gh/google/code-prettify@master/loader/run_prettify.js"))
         hiccup/html
         page/html5
         (spit (ns->out-filename namespace)))))

;; ;; We watch the catalogue of notes, and render them when it changes.
;; (add-watch
;;  ns->notes :render
;;  (fn [key reference old-state new-state]
;;    (doseq [[namespace new-notes] new-state]
;;      (render-ns! namespace new-notes))))


;; Printing a note results in rendering it,
;; and showing the rendered value in the browser.
(defmethod print-method Note [anote _]
  (let [file (str (File/createTempFile "rendered" ".html"))]
    (->> anote
         render!
         (spit file))
    (future (sh "firefox" file))
    #_(browse-url file)))

;; What to do with :status ?


;; (def kind->behaviour
;;   {:fn  {:eval            true
;;          :naturally-fresh true
;;          :rendered        false}
;;    :out {:eval            false
;;          :naturally-fresh false
;;          :rendered        true}})

;; (defn update-note-change [prev-note note]
;;   (assoc note
;;          :change (not= (:forms prev-note)
;;                        (:forms note))))

;; (defn update-note-fresh [note]
;;   (assoc note
;;          :fresh (or (-> note :kind kind->behaviour :naturally-fresh)
;;                     (and (:fresh prev-note)
;;                          (not changed)))))

;; (defn update-notes-transitive-change [notes]
;;   (map (fn [note transitive-change]
;;          (assoc note :transitive-change transitive-change))
;;        notes
;;        (->> notes
;;             (map :changed)
;;             (reductions #(or %1 %2)))))

;; (defn update-notes [prev-notes notes]
;;   (let [notes1         (map updated-note
;;                             (concat prev-notes
;;                                     (repeat nil))
;;                             notes)
;;         sticky-changes (->> notes1
;;                             (map :changed)
;;                             (reductions #(or %1 %2)))]
;;     (->> notes1
;;          (map (fn [sticky-change note]
;;                 (assoc note :changed sticky-change))
;;               sticky-changes)
;;          (map (fn [note]
;;                 (assoc note
;;                        :fresh (or (-> note :kind kind->behaviour :naturally-fresh)
;;                                   (not (:changed note)))))))))

;; (defn update-ns-notes! []
;;   (swap! ns->notes
;;          update
;;          *ns*
;;          #(update-notes
;;            %
;;            (ns-notes *ns*))))

;; (defn refresh-note! [anote]
;;   (println [:refreshing anote])
;;   (if (-> anote :fresh not)
;;     (do
;;       (println "__________________")
;;       (assoc anote
;;              :fresh true
;;              :value (if  (-> anote :kind kind->behaviour :rendered)
;;                       (->> anote
;;                            :forms
;;                            (map eval))
;;                       (doseq [form (:forms anote)]
;;                         (eval form)))))
;;     anote))

;; (defn refresh-ns! []
;;   (update-ns-notes!)
;;   (-> ns->notes
;;       (swap! update *ns*
;;              (partial mapv refresh-note!))
;;       (get *ns*)))

;; (defn rerender! []
;;   (->> (refresh-ns!)
;;        pprint-and-return
;;        (map (fn [anote]
;;               (into [:div
;;                      (println anote)
;;                      (->> anote
;;                           :forms
;;                           (map (fn [form]
;;                                  [:p
;;                                   [:code {:class "prettyprint"}
;;                                    (->> form
;;                                         pp/pprint
;;                                         with-out-str
;;                                         cljfmt.core/reformat-string)]]))
;;                           (into [:div
;;                                  {:style "background-color:#eeeeee;"}]))
;;                      [:p (if (:fresh anote)
;;                            ""
;;                            "[?]")]]
;;                     (:value anote))))
;;        (into [:div])
;;        (vector :body
;;                {:style "background-color:#cccccc; align:center"})
;;        (vector :head
;;                (page/include-js
;;                 "https://cdn.jsdelivr.net/gh/google/code-prettify@master/loader/run_prettify.js"))
;;        hiccup/html
;;        page/html5
;;        (spit "resources/public/index.html")))



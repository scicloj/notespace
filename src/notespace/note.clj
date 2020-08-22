(ns notespace.note
  (:require [notespace.reader :as reader]
            [notespace.util :as u]
            [rewrite-clj.node]
            [notespace.source :as source]
            [notespace.context :as ctx]
            [notespace.events :as events]
            [notespace.view :as view]
            [notespace.state :as state]))

;; A note has a static part: a kind, possibly a label, a collection of forms, and the reader metadata,
;; and a dynamic part: a value, a rendering and a status.
(defrecord Note [kind label forms metadata
                 value rendering status])

;; TODO: Where is is used?
(defn note->index [namespace note]
  (->> note
       :metadata
       :line
       (state/sub-get-in :ns->line->index namespace)))

;; We can collect all toplevel forms in a namespace,
;; together with the reader metadata.
(defn ->ns-topforms-with-metadata [namespace]
  (->> namespace
       source/ns->source-filename
       reader/file->topforms-with-metadata
       (filter (comp :line meta))))

(defn ns-topform? [topform]
  (and (sequential? topform)
       (-> topform
           first
           (= 'ns))))

(defn strings-topform? [topform]
  (and (sequential? topform)
       (-> topform
           first
           string?)))

(defn kinds-set []
  (keys (state/sub-get-in :kind->behaviour)))

(defn metadata->kind [m]
  (->> (kinds-set)
       (filter m)
       first))

(defn topform-with-metadata->kind [tfwm]
  (or (-> tfwm meta metadata->kind)
      (if (strings-topform? tfwm)
        :md
        :naive)))

(defn topform-with-metadata->forms [tfwm]
  (if (-> tfwm meta :multi)
    tfwm
    [tfwm]))

(defn note-with-updated-rendering [note]
  (assoc note
         :rendering (view/note->hiccup note (:value note))))

;; Each toplevel form can be converted to a Note.
(defn topform-with-metadata->Note [tfwm]
  (let [m (meta tfwm)]
    (when-not (ns-topform? tfwm)
      (-> (->Note (topform-with-metadata->kind tfwm)
                  (:label m)
                  (topform-with-metadata->forms tfwm)
                  m
                  :value/not-ready
                  nil
                  {:stage :initial})
          note-with-updated-rendering))))

;; Thus we can collect all notes in a namespace.
(defn ns-notes [namespace]
  (->> namespace
       ->ns-topforms-with-metadata
       (map topform-with-metadata->Note)
       (filter some?)))

(defn note-evaluation [note]
  (try
    (->> note
         :forms
         (cons 'do)
         eval)
    (catch Exception e
      (throw (ex-info "Note evaluation failed."
                      {:note      note
                       :exception e}))) ))

(defn evaluate-note [note]
  (-> note
      (assoc :value (note-evaluation note)
             :rendering nil
             :status {:stage :evaluated})
      note-with-updated-rendering))

;; A note is realized by realizing all its pending values and rendering them.
(defn realize-note [note]
  (let [value     (note-evaluation note)
        renderer  (state/sub-get-in :kind->behaviour (:kind note) :value->hiccup)
        rendering (-> value u/realize renderer)]
    (assoc note
           :value value
           :rendering rendering)))

(defn realizing-note [note]
  (assoc note
         :status {:stage :realizing}))

(defn realized-note [note]
  (assoc note
         :status {:rendered (view/note->hiccup
                             note
                             (-> note :value deref))
                  :stage :realized}))

;; TODO: Rethink
(defn different-note? [old-note new-note]
  (or (->> [old-note new-note]
           (map (comp :source :metadata))
           (apply not=))
      (->> [old-note new-note]
           (map (juxt :kind :forms))
           (apply not=))))

(defn merge-note [old-note new-note]
  (if (different-note? old-note new-note)
    new-note
    (merge old-note
           (select-keys new-note [:metadata]))))

(defn merge-notes [old-notes
                   new-notes]
  (mapv merge-note
        (concat old-notes (repeat nil))
        new-notes))

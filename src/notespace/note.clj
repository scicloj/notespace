(ns notespace.note
  (:require [notespace.reader :as reader]
            [rewrite-clj.node]
            [notespace.source :as source]
            [notespace.view :as view]
            [notespace.state :as state]
            [notespace.util :as u]
            [notespace.kinds :as kinds]))

;; A note has a static part: a kind, possibly a label, a collection of forms, and the reader metadata,
;; and a dynamic part: a value, a realized-value and a stage.
(defrecord Note [kind label forms metadata
                 value realized-value stage])

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

(defn void-by-default-topform? [topform]
  (and (sequential? topform)
       (-> topform
           first
           ('#{def defonce defn defmacro require import}))))

(defn midje-topform? [topform]
  (and (sequential? topform)
       (-> topform
           first
           (#{'fact 'facts}))))

(defn vector-beginning-with-keyword-topform? [topform]
  (and (vector? topform)
       (-> topform
           first
           keyword?)))

(defn kinds-set []
  (into #{} (map first) (-> kinds/kind->behaviour methods seq)))

(defn metadata->kind [m]
  (some->> m
           :tag
           resolve
           deref
           ((kinds-set))))

(defn topform-with-metadata->kind [tfwm]
  (or
   (-> tfwm meta metadata->kind)
   (cond
     ;;
     (strings-topform? tfwm)
     :notespace.kinds/md-nocode
     ;;
     (void-by-default-topform? tfwm)
     :notespace.kinds/void
     ;;
     (midje-topform? tfwm)
     :notespace.kinds/midje
     ;;
     (vector-beginning-with-keyword-topform? tfwm)
     (->> tfwm first name (keyword "notespace.kinds"))
     ;;
     :else :notespace.kinds/naive)))

(defn topform-with-metadata->forms [tfwm]
  (if (-> tfwm meta :multi)
    tfwm
    [tfwm]))

;; Each toplevel form can be converted to a Note.
(defn topform-with-metadata->Note [tfwm]
  (let [m (meta tfwm)]
    (when-not (ns-topform? tfwm)
      (->Note (topform-with-metadata->kind tfwm)
              (:label m)
              (topform-with-metadata->forms tfwm)
              m
              :value/not-ready
              nil
              :initial))))

;; Thus we can collect all notes in a namespace.
(defn ns-notes [namespace]
  (->> namespace
       ->ns-topforms-with-metadata
       (map topform-with-metadata->Note)
       (filter some?)))

(defprotocol Acceptable
  (accept! [value namespace idx]))

(defn accept
  "Accept a freshly evaluated note value,
  possibly invoking some special actions
  according to the nature of this value."
 [value namespace idx]
  (accept! value namespace idx)
  value)

(defn note-evaluation [namespace idx note]
  (try
    (-> note
        :forms
        (->> (cons 'do))
        eval
        (accept namespace idx))
    (catch Exception e
      (print (ex-info "Note evaluation failed."
                      {:note      note
                       :exception e}))
      ::failed)))

(defn evaluated-note [namespace idx note]
  (let [evaluation-callback-fn (state/sub-get-in :config :evaluation-callback-fn)
        in-eval-count-down-fn (state/sub-get-in :config :in-eval-count-down-fn)
        start-time (System/currentTimeMillis)
        expected-duration-in-s (/ (or (:duration note ) 0) 1000.0)]
    (evaluation-callback-fn idx
                            (count (ns-notes namespace))
                            note
                            )
    (when (> expected-duration-in-s 1)
      (future (doseq [x (reverse (range expected-duration-in-s))]
                (in-eval-count-down-fn x)
                (Thread/sleep 1000))))
    (let [value (note-evaluation namespace idx note)
          stage (if (= value ::failed) :failed :evaluated)
          duration (- (System/currentTimeMillis) start-time)]
      (-> note
          (assoc  :value value
                  :stage stage
                  :duration duration
                  )))))

(defn realizing-note [note]
  (assoc
   note
   :stage :realizing))

(defn realized-note [note]
  (assoc
   note
   :stage :realized
   :realized-value (-> note :value u/realize)))

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

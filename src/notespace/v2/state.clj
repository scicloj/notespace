(ns notespace.v2.state)

(def state
  (atom {}))

(defn reset-state! []
  (swap!
   state assoc
   ;; global configuration:
   :config {}
   ;; namespace specific configuration:
   :ns->config {}
   ;; one way to write a form is using one of several note symbols,
   ;; that have corresponding note kinds.
   ;; E.g., a form of the form (note-md ...) is a note form of kind :md.
   ;; This matching note-symbol->kind is configurable:
   :note-symbol->kind {}
   ;; A note's kind determintes controls various parameters of its evaluation and rendering.
   :kind->behaviour {}
   ;; a catalogue of notes, holding a vector of notes per namespace:
   :ns->notes {}
   ;; a catalogue of note states, holding a vector of note-states per namespace:
   :ns->note-states {}
    ;; for every line of code,
   ;; the index of the corresponding note in the sequence of notes,
   ;; if that line happens to lie inside a note:
   :ns->line->index {}
   ;; the indices of every note's label appearances in the sequence:
   :ns->label->indices {}
   ;; the last notespace rendered:
   :last-ns-rendered nil
   ;; We keep track of changes in source files corresponding to namespaces.
   :ns->last-modification {}))

(defn get-in-state [path]
  (get-in @state path))

(defn assoc-in-state! [& paths-and-values]
  (swap! state
         (fn [s0]
           (reduce (fn [s1 [path value]]
                     (assoc-in s1 path value))
                   s0
                   (partition 2 paths-and-values)))))

(defn update-in-state! [& paths-and-fns]
  (swap! state
         (fn [s0]
           (reduce (fn [s1 [path f]]
                     (update-in s1 path f))
                   s0
                   (partition 2 paths-and-fns)))))

(defn config
  ([]
   (config nil))
  ([path]
   (get-in-state (concat [:config] path))))

(defn ns->config
  ([namespace]
   (ns->config namespace nil))
  ([namespace path]
   (get-in-state (concat [:ns->config namespace] path))))

(defn note-symbol->kind [note-symbol]
  (get-in-state [:note-symbol->kind note-symbol]))

(defn ns->notes [namespace]
  (get-in-state [:ns->notes namespace]))

(defn ns->note-states [namespace]
  (get-in-state [:ns->note-states namespace]))

(defn ns->note [namespace idx]
  (get-in-state [:ns->notes namespace idx]))

(defn ns->note-state [namespace idx]
  (get-in-state [:ns->note-states namespace idx]))

(defn ns->line->index [namespace line]
  (get-in-state [:ns->line->index namespace line]))

(defn ns->note->index [namespace anote]
  (->> anote
       :metadata
       :line
       (ns->line->index namespace)))

(defn kind->behaviour
  ([]
   (get-in-state [:kind->behaviour]))
  ([kind]
   (get-in-state [:kind->behaviour kind])))

(defn last-ns-rendered []
  (get-in-state [:last-ns-rendered]))

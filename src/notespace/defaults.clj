(ns notespace.defaults
  (:require [hiccup.core :as hiccup]
            [notespace.view :as view]))

(def kind->behaviour
  {:code      {:render-src?    true
               :value->hiccup view/value->hiccup}
   :md        {:render-src?    false
               :value->hiccup view/mds->hiccup}
   :as-md     {:render-src?    true
               :value->hiccup view/mds->hiccup}
   :hiccup    {:render-src?    false
               :value->hiccup (fn [h] (hiccup/html h))}
   :as-hiccup {:render-src?    true
               :value->hiccup (fn [h] (hiccup/html h))}
   :void      {:render-src?    true
               :value->hiccup (constantly nil)}})

(def initial-state
  {;; global configuration:
   :config                {}
   ;; namespace specific configuration:
   :ns->config            {}
   ;; one way to write a form is using one of several note symbols,
   ;; that have corresponding note kinds.
   ;; E.g., a form of the form (note-md ...) is a note form of kind :md.
   ;; This matching note-symbol->kind is configurable:
   :note-symbol->kind     {}
   ;; A note's kind determintes controls various parameters of its evaluation and rendering.
   :kind->behaviour       kind->behaviour
   ;; a catalogue of notes, holding a vector of notes per namespace:
   :ns->notes             {}
   ;; a catalogue of note states, holding a vector of note-states per namespace:
   :ns->note-states       {}
   ;; for every line of code,
   ;; the index of the corresponding note in the sequence of notes,
   ;; if that line happens to lie inside a note:
   :ns->line->index       {}
   ;; the indices of every note's label appearances in the sequence:
   :ns->label->indices    {}
   ;; the last notespace rendered:
   :last-ns-rendered      nil
   ;; We keep track of changes in source files corresponding to namespaces.
   :ns->last-modification {}})


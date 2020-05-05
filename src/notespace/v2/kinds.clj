(ns notespace.v2.kinds
  (:require [notespace.v2.state :as state]
            [notespace.v2.view :as view]
            [hiccup.core :as hiccup]))

(defmacro defkind [note-symbol kind behaviour]
  `(do (state/assoc-in-state!
        [:kind->behaviour ~kind] ~behaviour
        [:note-symbol->kind (quote ~note-symbol)] ~kind)
       (defmacro ~note-symbol [& forms#]
         nil)
       ;; https://stackoverflow.com/questions/20831029/how-is-it-possible-to-intern-macros-in-clojure
       (intern (quote notespace.v2.note)
               (with-meta (quote ~note-symbol)
                 {:macro true})
               (deref (var ~note-symbol)))))


(defn define-base-kinds! []
  (defkind note
    :code {:render-src?    true
           :value-renderer view/value->hiccup})
  (defkind note-md
    :md   {:render-src?    false
           :value-renderer view/md->hiccup})
  (defkind note-as-md
    :as-md   {:render-src?    true
              :value-renderer view/md->hiccup})
  (defkind note-hiccup
    :hiccup {:render-src?    false
             :value-renderer (fn [h] (hiccup/html h))})
  (defkind note-as-hiccup
    :as-hiccup {:render-src?    true
                :value-renderer (fn [h] (hiccup/html h))})
  (defkind note-void
    :void {:render-src?    true
           :value-renderer (constantly nil)}))


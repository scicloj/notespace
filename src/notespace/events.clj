(ns notespace.events
  (:require [cljfx.api :as fx]))

(defmulti handle :event/type)

(defmethod handle :default [event]
  (throw (ex-info "Unrecognized event"
                  {:event event})))

(defmethod handle ::reset [{:keys [fx/context initial-state]}]
  {:context (fx/reset-context
             context
             initial-state)})

(defmethod handle ::on-result [{:keys [fx/context idx value]}]
  {:context context})

(defmethod handle ::file-modified [{:keys [fx/context namespace modification-time]}]
  {:context (fx/swap-context
             context
             #(-> %
                  (assoc-in
                   [:ns->last-modification namespace] modification-time)
                  (assoc :last-ns-handled namespace)))})

(defmethod handle ::assoc-notes [{:keys [fx/context namespace notes line->index label->indices]}]
  {:context (fx/swap-context
             context
             #(-> %
                  (assoc-in [:ns->notes namespace] notes)
                  (assoc-in [:ns->line->index namespace] line->index)
                  (assoc-in [:ns->label->indices namespace] label->indices)
                  (assoc :last-ns-handled namespace)))})

(defmethod handle ::update-note [{:keys [fx/context namespace idx f]}]
  {:context (if idx
              (fx/swap-context
               context
               #(-> %
                    (update-in [:ns->notes namespace idx]
                               f)
                    (assoc :last-ns-handled namespace)))
              context)})

(defmethod handle ::assoc-input [{:keys [fx/context symbol value]}]
  {:context (fx/swap-context
             context
             #(-> %
                  (assoc-in [:ns->inputs symbol]
                            value)))})

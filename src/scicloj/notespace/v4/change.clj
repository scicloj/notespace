(ns scicloj.notespace.v4.change
  (:require [clojure.pprint :as pp]
            [scicloj.notespace.v4.path :as v4.path]
            [scicloj.notespace.v4.state :as v4.state]
            [scicloj.notespace.v4.log :as v4.log]
            [editscript.core :as editscript]))


(defn set-current-path [state path]
  (assoc state
          :current-path
          path))

(defn set-request-details [state request-id details]
  (assoc-in state
            [:request-id->details request-id]
            details))

(defn update-request-details [state request-id f & args]
  (update-in state
             [:request-id->details request-id]
             #(f % args)))

(defn set-last-evaluated-note [state note]
  (assoc state
         :last-evaluated-note
         note))

(defn edit-notes [state path edits]
  (update-in state
             [:path->notes path]
             editscript/patch (editscript/edits->script edits)))

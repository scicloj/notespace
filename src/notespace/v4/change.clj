(ns notespace.v4.change
  (:require [clojure.pprint :as pp]
            [notespace.v4.path :as v4.path]
            [notespace.v4.state :as v4.state]
            [editscript.core :as editscript]))


(defn set-current-path [state path]
  (assoc state
          :current-path
          path))

(defn set-request-path [state request-id path]
  (assoc-in state
            [:request-id->path request-id]
            path))

(defn set-last-value [state value]
  (assoc state
         :last-value
         value))

(defn edit-notes [state path edits]
  (update-in state
             [:path->notes path]
             editscript/patch (editscript/edits->script edits)))

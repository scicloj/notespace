(ns notespace.v4.frontend.change
  (:require [notespace.v4.frontend.engine :as v4.frontend.engine]
            [notespace.v4.view :as v4.view]))

(defn reset-frontend-header! [{:keys [messages last-value]}]
  (v4.frontend.engine/reset-header!
   (v4.view/->header
    messages
    last-value)))

(defn reset-frontend! [{:keys [current-notes]
                        :as details}]
  (reset-frontend-header! details)
  (v4.frontend.engine/reset-widgets!)
  (->> current-notes
       (v4.frontend.engine/sync-widgets! :notespace.v4.note/id
                                         v4.view/note->hiccup)))

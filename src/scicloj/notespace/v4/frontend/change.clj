(ns scicloj.notespace.v4.frontend.change
  (:require [scicloj.notespace.v4.frontend.engine :as v4.frontend.engine]
            [scicloj.notespace.v4.view :as v4.view]))

(defn reset-frontend-header! [details]
  (v4.frontend.engine/reset-header!
   (v4.view/->header details)))

(defn reset-frontend! [{:keys [current-notes]
                        :as details}]
  (reset-frontend-header! details)
  (v4.frontend.engine/reset-widgets!)
  (->> current-notes
       (v4.frontend.engine/sync-widgets! :scicloj.notespace.v4.note/id
                                         v4.view/note->hiccup)))

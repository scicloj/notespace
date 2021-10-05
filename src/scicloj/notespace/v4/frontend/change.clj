(ns scicloj.notespace.v4.frontend.change
  (:require [scicloj.notespace.v4.frontend.engine :as v4.frontend.engine]
            [scicloj.notespace.v4.view :as v4.view]
            [scicloj.notespace.v4.log :as v4.log]))

(defn reset-frontend-header! [details]
  (v4.frontend.engine/reset-header!
   (v4.view/->header details)))

(defn reset-frontend! [{:keys [current-notes]
                        :as details}]
  (reset-frontend-header! details)
  (v4.frontend.engine/reset-widgets!)
  (->> current-notes
       (mapcat (fn [note]
                 [[:view/source note]
                  [:view/value note]]))
       (v4.frontend.engine/sync-widgets! (fn [[part note]]
                                           (+ (:scicloj.notespace.v4.note/id note)
                                              (case part
                                                :view/source 0
                                                :view/value 0.1)))
                                         v4.view/note->hiccup)))

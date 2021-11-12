(ns scicloj.notespace.v4.frontend.change
  (:require [scicloj.notespace.v4.frontend.engine :as v4.frontend.engine]
            [scicloj.notespace.v4.view :as v4.view]
            [scicloj.notespace.v4.log :as v4.log]
            [scicloj.notespace.v4.config :as v4.config]))

(defn reset-frontend-header! [details]
  (v4.frontend.engine/reset-header!
   (v4.view/->header details)))

(defn reset-frontend! [{:keys [current-notes]
                        :as details}]
  (let [{:keys [header? notes?]} @v4.config/*config]
    (when header?
      (reset-frontend-header! details))
    (when notes?
      (->> current-notes
           (mapcat (fn [note]
                     [[:view/source note]
                      [:view/state note]]))
           (v4.frontend.engine/sync-widgets! (fn [[part note]]
                                               (case part
                                                 :view/source (:scicloj.notespace.v4.note/id note)
                                                 :view/state  (+ (:scicloj.notespace.v4.note/id note)
                                                                 0.1)))
                                             v4.view/note->hiccup)))))



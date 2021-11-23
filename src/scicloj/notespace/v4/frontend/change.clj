(ns scicloj.notespace.v4.frontend.change
  (:require [scicloj.notespace.v4.frontend.engine :as v4.frontend.engine]
            [scicloj.notespace.v4.view :as v4.view]
            [scicloj.notespace.v4.log :as v4.log]
            [scicloj.notespace.v4.config :as v4.config]
            [scicloj.notespace.v4.note :as v4.note]
            [clojure.string :as string]))

(defn reset-frontend-header! [details]
  (v4.frontend.engine/reset-header!
   (v4.view/->header details)))

(defn reset-frontend! [{:keys [current-notes last-evaluated-note messages]
                        :as details}]
  (let [{:keys [header? notebook? last-eval? debug?]} @v4.config/*config]
    (when header?
      (reset-frontend-header! details))
    (when (and last-eval?
               last-evaluated-note)
      (->> [[:view/state last-evaluated-note]]
           (v4.frontend.engine/sync-widgets!
            :last-eval
            false
            (fn [_] (java.util.UUID/randomUUID))
            v4.view/note->hiccup)))
    (when debug?
      (->> messages
           reverse
           (v4.frontend.engine/sync-widgets!
            :debug
            false
            (fn [_] (java.util.UUID/randomUUID))
            identity)))
    (let [note-modes
          (if (not notebook?)
            []
            (let [notes-separated (->> current-notes
                                       (partition-by v4.note/separator?))]
              (if (= 1 (count notes-separated))
                (do (->> current-notes
                         (mapcat (fn [note]
                                   [[:view/source note]
                                    [:view/state note]]))
                         (v4.frontend.engine/sync-widgets!
                          :notespace
                          false
                          (fn [[part note]]
                            (case part
                              :view/source (:scicloj.notespace.v4.note/id note)
                              :view/state  (+ (:scicloj.notespace.v4.note/id note)
                                              0.1)))
                          v4.view/note->hiccup))
                    [:notespace])
                (->> notes-separated
                     (partition 2)
                     (map (fn [[title-notes notes]]
                            (let [mode (-> title-notes
                                           first
                                           :source
                                           (string/replace #"^;*\s *# " "")
                                           keyword)]
                              (->> (concat title-notes notes)
                                   (mapcat (fn [note]
                                             [[:view/source note]
                                              [:view/state note]]))
                                   (v4.frontend.engine/sync-widgets!
                                    mode
                                    false
                                    (fn [[part note]]
                                      (case part
                                        :view/source (:scicloj.notespace.v4.note/id note)
                                        :view/state  (+ (:scicloj.notespace.v4.note/id note)
                                                        0.1)))
                                    v4.view/note->hiccup))
                              mode)))
                     doall))))]
      (v4.frontend.engine/restrict-modes!
       (concat note-modes
               (when last-eval? [:last-eval])
               (when debug? [:debug]))))
    (v4.frontend.engine/broadcast-widgets!)))


(v4.note/separator? {:comment? true
                     :source   ";; # notespace"})

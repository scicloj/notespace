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

(defn title-note? [note]
  (some?
   (and (:comment? note)
        (-> note
            :source
            (string/split #"\n")
            first
            (->> (re-matches #"^;*\s*#\s*.*"))))))

(defn title-note->title [note]
  (-> note
      :source
      (string/split #"\n")
      first
      (string/replace #"^;*\s*#" "")
      string/trim))

(defn reset-frontend! [{:keys [current-notes last-evaluated-note messages]
                        :as   details}]
  (let [{:keys [header? notebook? last-eval? debug? note-layout]} @v4.config/*config]
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
            (let [first-note-title?        (->> current-notes
                                                first
                                                title-note?)
                  notes-beginning-with-sep (if first-note-title?
                                             current-notes
                                             (cons {:source   ";; # notespace"
                                                    :comment? true
                                                    :omit?    true}
                                                   current-notes))
                  notes-separated          (->> notes-beginning-with-sep
                                                (partition-by title-note?))]
              (->> notes-separated
                   (partition 2)
                   (map (fn [[title-notes notes]]
                          (let [mode (-> title-notes
                                         first
                                         title-note->title)]
                            (->> (concat title-notes notes)
                                 (filter (complement :omit?))
                                 (mapcat (fn [note]
                                           (case note-layout
                                             :vertical [[:view/source note]
                                                        [:view/state note]]
                                             :horizontal [[:view/both note]])))
                                 (v4.frontend.engine/sync-widgets!
                                  mode
                                  false
                                  (fn [[part note]]
                                    (case part
                                      :view/source (:scicloj.notespace.v4.note/id note)
                                      :view/state  (+ (:scicloj.notespace.v4.note/id note)
                                                      0.1)
                                      :view/both (+ (:scicloj.notespace.v4.note/id note)
                                                    0.2)))
                                  v4.view/note->hiccup))
                            mode)))
                   doall)))]
      (v4.frontend.engine/restrict-modes!
       (concat note-modes
               (when last-eval? [:last-eval])
               (when debug? [:debug]))))
    (v4.frontend.engine/broadcast-widgets!)))



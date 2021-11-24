(ns scicloj.notespace.v4.run
  (:require [scicloj.notespace.v4.read :as v4.read]
            [scicloj.notespace.v4.view :as v4.view]
            [scicloj.notespace.v4.frontend.engine :as v4.frontend.engine]
            [scicloj.notespace.v4.events.pipeline :as v4.pipeline]))

(defn update-ns! [path]
  (v4.pipeline/process-event
   {:event/type :scicloj.notespace.v4.events.handle/buffer-update
    :path       path}))

(defn run-ns! [path]
  (update-ns! path)
  (let [widgets (->> path
                     slurp
                     v4.read/->safe-notes
                     (map (fn [note]
                            (assoc note
                                   :value (->> note :form eval)
                                   :status :evaluated)))
                     (mapcat (fn [note]
                               [[:view/source note]
                                [:view/state note]])))]
    (future
      (Thread/sleep 50)
      (v4.frontend.engine/sync-widgets!
       :notespace
       false
       (fn [[part note]]
         (case part
           :view/source (:scicloj.notespace.v4.note/id note)
           :view/state  (+ (:scicloj.notespace.v4.note/id note)
                           0.1)))
       v4.view/note->hiccup
       widgets))))


(comment
  (update-ns! "dummy.clj")
  (run-ns! "dummy.clj"))

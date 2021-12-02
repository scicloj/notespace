(ns scicloj.notespace.v4.run
  (:require [scicloj.notespace.v4.read :as v4.read]
            [scicloj.notespace.v4.view :as v4.view]
            [scicloj.notespace.v4.frontend.change :as v4.frontend.change]
            [scicloj.notespace.v4.frontend.engine :as v4.frontend.engine]
            [scicloj.notespace.v4.events.pipeline :as v4.pipeline]))

(defn update-ns! [path]
  (v4.pipeline/process-event
   {:event/type :scicloj.notespace.v4.events.handle/buffer-update
    :path       path}))

(defn uuid [] (.toString (java.util.UUID/randomUUID)))

(defn run-ns! [path]
  (update-ns! path)
  (let [notes (->> path
                   slurp
                   v4.read/->safe-notes
                   (map (fn [note]
                          (assoc note
                                 :value (->> note :form eval)
                                 :status :evaluated)))
                   doall)]
   (future
     (Thread/sleep 200)
     (v4.frontend.change/reset-frontend!
      {:current-notes       notes
       :last-evaluated-note (last notes)
       :messages            []})
     (println [:done path]))))


(comment
  (update-ns! "dummy.clj")
  (run-ns! "dummy.clj"))

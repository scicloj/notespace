(ns notespace.renderers.gorilla-notes
  (:require [gorilla-notes.core :as gn]
            [cljfx.api :as fx]))

(defonce server (atom nil))

(defn init []
  (when-not @server
    (reset! server true)
    (gn/start-server!))
  (gn/reset-notes!)
  (gn/merge-new-options! {:notes-in-cards? false
                          :header?         true
                          :reverse-notes?  false}))

(defn browse []
  (gn/browse-default-url))

(defn renderer [old-ctx new-ctx]
  (future
    (when-let [namespace (fx/sub-val new-ctx :last-ns-handled)]
      (let [[old-notes new-notes]             (->> [old-ctx new-ctx]
                                                   (map (fn [ctx]
                                                          (fx/sub-val
                                                           ctx
                                                           #(get-in % [:ns->notes namespace])))))
            [old-note-states new-note-states] (->> [old-ctx new-ctx]
                                                   (map (fn [ctx]
                                                          (fx/sub-val
                                                           ctx
                                                           #(get-in % [:ns->note-states namespace])))))]
        (->> (map vector
                  (range)
                  (concat old-notes (repeat nil))
                  (concat old-note-states (repeat nil))
                  new-notes
                  new-note-states)
             (run!
              (fn [[idx old-note old-note-state new-note new-note-state]]
                (println [:dbg idx new-note])
                (when-not
                    (and (= old-note new-note)
                         (= old-note-state new-note-state))
                  (println [:adding! idx new-note])
                  (gn/assoc-note!
                   idx
                   (:rendered new-note-state))))))
        (Thread/sleep 100)
        (gorilla-notes.communication/broadcast-content-ids!)))))


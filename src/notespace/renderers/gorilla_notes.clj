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
                          :header?         false
                          :reverse-notes?  false}))

(defn browse []
  (gn/browse-default-url))

(defn renderer [old-ctx new-ctx]
  #_(println [:rendering])
  (when-let [namespace (fx/sub-val new-ctx :last-ns-handled)]
    (let [[old-notes new-notes]             (->> [old-ctx new-ctx]
                                                 (map (fn [ctx]
                                                        (fx/sub-val
                                                         ctx
                                                         #(get-in % [:ns->notes namespace])))))
          new-things                        (->> (map vector
                                                      (range)
                                                      (concat old-notes (repeat nil))
                                                      new-notes)
                                                 (filter
                                                  (fn [[_ old-note new-note]]
                                                    (not (= old-note new-note)))))
          [old-n new-n]                     (->> [old-notes new-notes]
                                                 (map count))]
      (->> new-things
           (run!
            (fn [[idx _ new-note]]
              (gn/assoc-note!
               idx
               (:rendering new-note)
               :broadcast? false))))
      #_(println [:old old-n :new new-n])
      (when (> old-n new-n)
        (gn/drop-tail! (- old-n new-n)
                       :broadcast? false))
      (Thread/sleep 100)
      (gn/broadcast-content-ids!))))

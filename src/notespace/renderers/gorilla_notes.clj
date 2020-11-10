(ns notespace.renderers.gorilla-notes
  (:require [gorilla-notes.core :as gn]
            [cljfx.api :as fx]
            [notespace.view :as view]
            [notespace.note :as note]
            [notespace.actions :as actions]
            [notespace.util :as u]
            [clojure.core.async :as async]))

(defonce server (atom nil))

(defn header-and-footer-config [anamespace]
  (let [{:keys [header footer]} (view/header-and-footer
                                 anamespace)]
    {:custom-header header
     :custom-footer footer}))

(defn init []
  (when-not @server
    (reset! server true)
    (gn/start-server!))
  (gn/reset-notes!)
  (gn/merge-new-options! (merge
                          {:notes-in-cards? false
                           :header?         false
                           :reverse-notes?  false}
                          (header-and-footer-config
                           nil)))
  (gn/watch-inputs! actions/assoc-input!))

(def change?
  (atom false))

(defonce last-ns-rendered
  (atom nil))

(defn refresh-view []
  (reset! change? false)
  (gn/broadcast-content-ids!)
  (future
    (Thread/sleep 100) ; avoiding gorilla-notes sync race conditions
    (gn/merge-new-options! (header-and-footer-config
                            @last-ns-rendered))))

(defonce periodically-refresh-view
  (async/go-loop []
    (async/<! (async/timeout 200))
    (when @change?
      (refresh-view))
    (recur)))

(defn browse []
  (gn/browse-default-url))

(defn rendering [ctx anamespace idx]
  (view/note->hiccup
   (fx/sub-val ctx get-in [:ns->notes anamespace idx])))

(defn renderer [old-ctx new-ctx]
  (when-let [anamespace (fx/sub-val new-ctx :last-ns-handled)]
    ;; Checking if the actively handled anamespace has changed.
    (when (not= anamespace @last-ns-rendered)
      (gn/reset-notes!)
      (reset! last-ns-rendered anamespace))
    ;;Checking if we are here due to a user input change.
    (if (not= (fx/sub-val old-ctx :inputs)
              (fx/sub-val new-ctx :inputs))
      ;; React to a user input change.
      (do
        (dotimes [idx (-> new-ctx
                          (fx/sub-val get-in [:ns->notes anamespace])
                          count)]
          (actions/rerender-note! anamespace idx)))
      ;; Check for a change in notes.
      (let [[old-notes new-notes] (->> [old-ctx new-ctx]
                                       (map (fn [ctx]
                                              (fx/sub-val
                                               ctx
                                               get-in [:ns->notes anamespace]))))
            new-things            (->> (map vector
                                            (range)
                                            (concat old-notes (repeat nil))
                                            new-notes)
                                       (filter
                                        (fn [[_ old-note new-note]]
                                          (not (= old-note new-note)))))
            [old-n new-n]         (->> [old-notes new-notes]
                                       (map count))]
        (->> new-things
             (run!
              (fn [[idx _ _]]
                (gn/assoc-note!
                 idx
                 (fx/sub-ctx new-ctx rendering anamespace idx)
                 :broadcast? false))))
        (when (> old-n new-n)
          (gn/drop-tail! (- old-n new-n)
                         :broadcast? false))
        (reset! change? true)))))


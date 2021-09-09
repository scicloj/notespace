(ns notespace.v4.events.handle
  (:require [notespace.v4.state :as v4.state]
            [notespace.v4.log :as v4.log]
            [notespace.v4.merge :as v4.merge]
            [notespace.v4.read :as v4.read]
            [notespace.v4.note :as v4.note]
            [notespace.v4.path :as v4.path]
            [notespace.v4.status :as v4.status]
            [notespace.v4.view :as v4.view]))

(defn merge-new-notes [path new-notes]
  (v4.state/update-notes
   path
   (fn [current-notes]
     (v4.merge/merge-notes current-notes
                           new-notes))))

(declare handle-buffer-update)

(defn handle-buffer-update [{:keys [path buffer-snapshot]}]
  (some->> (or buffer-snapshot
               (slurp path))
           v4.read/->safe-notes
           (merge-new-notes path))
  (v4.status/add :updated-buffer
                 {:path path}))

(defn handle-eval [{:keys [path code buffer-snapshot request-id]
                    :as event}]
  (v4.status/add :handle-eval
                 {:event event})
  (when path
    (when buffer-snapshot
      (handle-buffer-update {:buffer-snapshot buffer-snapshot
                             :path path}))
    (v4.state/update-request-path request-id path)
    (let [current-notes (v4.state/current-notes path)
          region-notes  (some->> code
                                 v4.read/->safe-notes
                                 (map #(v4.note/mark-status
                                        %
                                        {:state      :evaluating
                                         :request-id request-id})))
          merged-notes (if region-notes
                         (v4.merge/merge-eval-region-notes
                          current-notes
                          region-notes)
                         current-notes)]
      (v4.state/update-notes
       path
       (fn [_] merged-notes)))
    (v4.status/add :started-eval
                   {:path       path
                    :request-id request-id})))

(defn handle-value [{:keys [request-id value]
                     :as   event}]
  (when-let [path (v4.state/request-path request-id)]
    (v4.state/reset-last-value value)
    (v4.state/update-notes
     path
     (fn [current-notes]
       (v4.merge/merge-value current-notes
                             event)))))


(defn handle [event]
  (case (:event-type event)
    :eval (handle-eval event)
    :buffer-update (handle-buffer-update event)
    :value (handle-value event)))


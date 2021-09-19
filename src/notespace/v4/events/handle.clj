(ns notespace.v4.events.handle
  (:require [notespace.v4.state :as v4.state]
            [notespace.v4.log :as v4.log]
            [notespace.v4.merge :as v4.merge]
            [notespace.v4.read :as v4.read]
            [notespace.v4.note :as v4.note]
            [notespace.v4.path :as v4.path]
            [notespace.v4.state :as v4.state]
            [notespace.v4.change :as v4.change]
            [notespace.v4.view :as v4.view]))

(defmulti handle :event/type)

(defmethod handle :default [event]
  (throw (ex-info "Unrecognized event"
                  {:event event})))

(defmethod handle ::buffer-update
  [{:keys [path buffer-snapshot state]}]
  (let [old-notes (v4.state/path-notes state path)
        new-notes (->> (or buffer-snapshot
                           (slurp path))
                       v4.read/->safe-notes)
        edits (some->> new-notes
                       (v4.merge/merge-notes old-notes))
        new-state (-> (if edits
                        (-> state
                            (v4.change/edit-notes path edits))
                        state)
                      (v4.change/set-current-path path))]
    (v4.state/add-formatted-message! :updated-buffer
                                     {:path path})
    new-state))

(defmethod handle ::eval
  [{:keys [path code buffer-snapshot request-id state]}]
  (when path
    (when buffer-snapshot
      (handle {:event/type ::buffer-snapshot
               :path            path}))
    (let [region-notes (some->> code
                                v4.read/->safe-notes
                                (map #(v4.note/mark-status
                                       %
                                       {:state      :evaluating
                                        :request-id request-id})))
          merged-notes (if region-notes
                         (v4.merge/merge-eval-region-notes
                          (v4.state/current-notes state)
                          region-notes)
                         [])
          new-state (-> state
                        (v4.change/set-request-path request-id path)
                        (v4.change/edit-notes path merged-notes))]
      (v4.state/add-formatted-message! :started-eval
                                          {:path       path
                                           :request-id request-id})
      new-state)))

(defmethod handle ::value
  [{:keys [request-id value state] :as event}]
  (let [new-state (-> (if-let [path (v4.state/request-path state request-id)]
                        ;; found the relevant eval request
                        ;; -- try edit the notes with the value
                        (do (v4.state/add-formatted-message! :updating-notes-with-value)
                            (v4.change/edit-notes
                             state
                             path
                             (v4.merge/merge-value (v4.state/current-notes state)
                                                   event)))
                        ;; else -- cannot edit the notes
                        state)
                      (v4.change/set-last-value value))]
    (v4.state/add-formatted-message! :updated-last-value)
    new-state))


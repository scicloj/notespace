(ns scicloj.notespace.v4.events.handle
  (:require [scicloj.notespace.v4.state :as v4.state]
            [scicloj.notespace.v4.log :as v4.log]
            [scicloj.notespace.v4.merge :as v4.merge]
            [scicloj.notespace.v4.read :as v4.read]
            [scicloj.notespace.v4.note :as v4.note]
            [scicloj.notespace.v4.path :as v4.path]
            [scicloj.notespace.v4.state :as v4.state]
            [scicloj.notespace.v4.change :as v4.change]
            [scicloj.notespace.v4.view :as v4.view]
            [scicloj.notespace.v4.kinds :as v4.kind]
            [clojure.string :as string]))

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
                                (map (fn [note]
                                       (if (:comment? note)
                                         note
                                         ;; else
                                         (v4.note/mark-status
                                          note
                                          {:state      :evaluating
                                           :request-id request-id})))))
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
                        ;; -- try to edit the notes with the value
                        (do (v4.state/add-formatted-message! :updating-notes-with-value)
                            (v4.change/edit-notes
                             state
                             path
                             (v4.merge/merge-value (v4.state/current-notes state)
                                                   event)))
                        ;; else -- cannot edit the notes
                        state)
                      (v4.change/set-last-value value))]
    (v4.state/add-formatted-message! :updated-last-value
                                     {:request-id request-id})
    new-state))


(defmethod handle ::error
  [{:keys [request-id err state] :as event}]
  (let [new-state (-> state
                      (v4.change/set-last-value
                       (-> [:div
                            [:p/markdown
                             (-> err
                                 (string/replace #"\n" "\n\n"))]]
                           (v4.kind/override v4.kind/hiccup))))]
    (v4.state/add-formatted-message! :handled-error
                                     {:request-id request-id
                                      :err err})
    new-state))

(defmethod handle ::done
  [{:keys [request-id state] :as event}]
  (let [new-state (-> (if-let [path (v4.state/request-path state request-id)]
                        ;; found the relevant eval request
                        ;; -- try to edit the notes with the value
                        (do (v4.state/add-formatted-message! :updating-notes-with-value)
                            (v4.change/edit-notes
                             state
                             path
                             (v4.merge/merge-done (v4.state/current-notes state)
                                                  event)))
                        ;; else -- cannot edit the notes
                        state))]
    (v4.state/add-formatted-message! :finished-handling-eval
                                     {:request-id request-id})
    new-state))

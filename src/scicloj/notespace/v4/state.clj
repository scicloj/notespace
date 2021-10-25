(ns scicloj.notespace.v4.state
  (:require [scicloj.notespace.v4.log :as v4.log]
            [scicloj.notespace.v4.view :as v4.view]
            [scicloj.notespace.v4.note :as v4.note]
            [scicloj.notespace.v4.frontend.change :as v4.frontend.change]
            [scicloj.notespace.v4.messages :as v4.messages]
            [scicloj.notespace.v4.path :as v4.path]
            [editscript.core :as editscript])
  (:import java.util.Date))

(def initial-state {:event-counter       0
                    :last-evaluated-note nil
                    :path->notes         {}
                    :request-id->details {}
                    :current-path        nil
                    :pipeline            nil})

(defonce *state (atom initial-state))


(defn init! []
  (reset! *state initial-state))

(defonce *messages (atom []))

(defn next-event-counter []
  (swap! *state update :event-counter inc)
  (:event-counter @*state))

(defn path-notes [state path]
  (-> state
      (get-in [:path->notes path])
      (or [])))

(defn current-notes [state]
  (path-notes state (:current-path state)))

(defn request-details [state request-id]
  (-> state
      :request-id->details
      (get request-id)))

(defn request-path [state request-id]
  (-> state
      (request-details request-id)
      :path))

(defn reset-frontend! []
  (v4.frontend.change/reset-frontend!
   {:messages            @*messages
    :last-evaluated-note (:last-evaluated-note @*state)
    :current-notes       (current-notes @*state)
    :current-path        (some-> @*state
                           :current-path
                           v4.path/path-relative-to-current-directory)
    :counts              (some-> @*state
                           current-notes
                           v4.note/notes->counts)}))


(defn reset-state! [state frontend-too?]
  (reset! *state state)
  (when frontend-too?
    (reset-frontend!)))

(defn pipeline []
  (:pipeline @*state))

(defn reset-pipeline! [pipeline]
  (swap! *state assoc :pipeline pipeline)
  (reset-frontend!))

(defn stop! []
  (swap! *state assoc :started? false))

(defn add-formatted-message!
  ([label]
   (add-formatted-message! label {}))
  ([label info]
   (swap! *messages v4.messages/add-formatted-message label info)
   (reset-frontend!)))


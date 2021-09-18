(ns notespace.v4.state
  (:require [notespace.v4.log :as v4.log]
            [notespace.v4.view :as v4.view]
            [notespace.v4.frontend.change :as v4.frontend.change]
            [notespace.v4.messages :as v4.messages]
            [editscript.core :as editscript])
  (:import java.util.Date))

(defonce *state (atom {:started?         false
                       :event-counter    0
                       :last-value       nil
                       :path->notes      {}
                       :request-id->path {}
                       :current-path        nil}))

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

(defn request-path [state request-id]
  (-> state
      :request-id->path
      (get request-id)))

(defn reset-frontend! []
  (v4.frontend.change/reset-frontend! {:messages      @*messages
                                       :last-value    (:last-value @*state)
                                       :current-notes (current-notes @*state)}))


(defn reset-state! [state]
  (reset! *state state)
  (reset-frontend!))

(defn started? []
  (:started? @*state))

(defn start! []
  (swap! *state assoc :started? true)
  (reset-frontend!))

(defn stop! []
  (swap! *state assoc :started? false))


(defn add-formatted-message!
  ([label]
   (add-formatted-message! label {}))
  ([label info]
   (swap! *messages v4.messages/add-formatted-message label info)
   (reset-frontend!)))


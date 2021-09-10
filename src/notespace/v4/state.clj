(ns notespace.v4.state
  (:require [notespace.v4.log :as v4.log]
            [notespace.v4.view :as v4.view]
            [gorilla-notes.core :as gn])
  (:import java.util.Date))

(defonce *state (atom {:started?         false
                       :gn-server        nil
                       :status-messages  []
                       :last-value       nil
                       :path->notes      {}
                       :request-id->path {}
                       :last-path nil
                       :event-counter    0
                       :watcher nil}))

(declare update-view)

(defn start []
  (swap! *state assoc :started? true)
  (update-view))

(defn started? []
  (:started? @*state))

(defn next-event-counter []
  (swap! *state update :event-counter inc)
  (:event-counter @*state))

(defn current-notes [path]
  (-> @*state
      (get-in [:path->notes path])))

(defn add-status [message]
  (swap! *state
         update :status-messages
         conj message)
  (gn/merge-new-options!
   {:custom-header (-> @*state
                       :status-messages
                       v4.view/messages->hiccup) 
    :custom-footer nil}))

(defn update-last-path [path]
  (swap! *state
         assoc :last-path path))

(defn update-request-path [request-id path]
  (swap! *state
         assoc-in [:request-id->path request-id]
         path)
  (update-last-path path))

(defn request-path [request-id]
  (-> @*state
      :request-id->path
      (get request-id)))

(defn last-path []
  (:last-path @*state))

(defn update-notes [path current-notes->merged-notes]
  (swap! *state
         update-in [:path->notes path]
         current-notes->merged-notes)
  (update-last-path path)
  (update-view))

(defn reset-last-value [value]
  (swap! *state assoc :last-value value)
  (update-view))

(defn last-value []
  (:last-value @*state))

(defn update-view []
  (v4.view/update-view
   (last-value)
   (current-notes (last-path))))


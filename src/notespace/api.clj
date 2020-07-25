(ns notespace.api
  (:require [notespace.note :as note]
            [notespace.state :as state]
            [notespace.init :as init]
            [notespace.live-reload :as live-reload]))

(defn init! []
  (init/init!))

(defn init-live-reload! []
  (live-reload/restart!)
  (live-reload/open-browser))

(defn check [pred & args]
  [(if (apply pred args)
     :PASSED
     :FAILED)
   (last args)])

(defn realize-note-at-line! [line]
  (note/read-notes-seq! *ns*)
  (some->> line
           (state/ns->line->index *ns*)
           (state/ns->note *ns*)
           (note/realize-note! *ns*))
  [[:realized {:ns   *ns*
               :line line}]
   #_(render-this-ns!)])

(defn realize-this-notespace! []
  (note/read-notes-seq! *ns*)
  (->> *ns*
       (state/ns->notes)
       (run! (partial note/realize-note! *ns*)))
  [[:realized {:ns *ns*}]
   #_(render-this-ns!)])

(defmacro D [& forms]
  (cons 'delay forms))


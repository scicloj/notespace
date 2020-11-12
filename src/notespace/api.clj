(ns notespace.api
  (:require [notespace.actions :as actions]
            [notespace.lifecycle :as lifecycle]
            [notespace.note :as note]
            [notespace.state :as state]
            [notespace.util :as u]
            [notespace.paths :as paths]
            [notespace.watch :as watch]
            [gorilla-notes.core :as gn]
            [notespace.source :as source]))

(def init lifecycle/init)

(defn init-with-browser []
  (init :open-browser? true))

(defn check [pred & args]
  [(if (apply pred args)
     :PASSED
     :FAILED)
   (last args)])

(defn reread-this-notespace []
  (actions/reread-notes! *ns*))

(defn eval-this-notespace []
  (actions/act-on-notes! *ns* [actions/eval-note!]))

(defn eval-and-realize-this-notespace []
  (actions/act-on-notes! *ns* [actions/eval-note!
                               actions/realize-note!]))

(defn eval-note-at-line [line]
  (actions/act-on-note-at-line! *ns* line [actions/eval-note!]))

(defn realize-note-at-line [line]
  (actions/act-on-note-at-line! *ns* line [actions/realize-note!]))

(defn eval-and-realize-note-at-line [line]
  (actions/act-on-note-at-line! *ns* line [actions/eval-note!
                                           actions/realize-note!]))

(defn eval-and-realize-notes-from-line [line]
  (actions/act-on-notes-from-line! *ns* line [actions/eval-note!
                                              actions/realize-note!]))

(defn render-static-html
  ([]
   (render-static-html nil))
  ([path]
   (let [path-to-use (or path (paths/ns->target-path *ns*))]
     (gn/render-current-state! path-to-use)
     (println [:rendered path-to-use]))))

(defmacro R [symbols & forms]
  `(reify clojure.lang.IDeref
     (deref [~'_]
       (when-let [inputs# (state/sub-get-in :inputs)]
         (let [{:keys [~@symbols]} inputs#]
           ~@forms)))))

(defn check [pred & args]
  [(if (apply pred args)
     :PASSED
     :FAILED)
   (last args)])

(defonce change-lock (atom false))

(defn eval-and-realize-notes-from-change
  ([]
   (eval-and-realize-notes-from-change *ns*))
  ([anamespace]
   (when (not @change-lock)
     (reset! change-lock true)
     (actions/eval-and-realize-notes-from-change! anamespace)
     (reset! change-lock false))))

(defonce namespaces-listening-to-changes
  (atom #{}))

(defn listen
  ([]
   (listen *ns*))
  ([anamespace]
   (swap! namespaces-listening-to-changes conj anamespace)))

(defn unlisten
  ([]
   (unlisten *ns*))
  ([anamespace]
   (swap! namespaces-listening-to-changes #(remove #{anamespace} %))))

(defonce listen-sleep
  (atom 300))

(defonce periodically-react-to-changes
  (future
    (while true
      (Thread/sleep @listen-sleep)
      (doseq [anamespace @namespaces-listening-to-changes]
        (eval-and-realize-notes-from-change anamespace)))))

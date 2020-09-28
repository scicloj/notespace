(ns notespace.api
  (:require [notespace.actions :as actions]
            [notespace.lifecycle :as lifecycle]
            [notespace.note :as note]
            [notespace.state :as state]
            [notespace.util :as u]
            [notespace.paths :as paths]
            [gorilla-notes.core :as gn]))

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
  (actions/eval-notes! *ns*))

(defn eval-note-at-line [line]
  (actions/eval-note-at-line! *ns* line))

(defn realize-note-at-line [line]
  (actions/realize-note-at-line! *ns* line))

(defn eval-and-realize-note-at-line [line]
  (actions/eval-and-realize-note-at-line! *ns* line))

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

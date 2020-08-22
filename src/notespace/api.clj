(ns notespace.api
  (:require [notespace.actions :as actions]
            [notespace.lifecycle :as lifecycle]))

(def init lifecycle/init)

(defn check [pred & args]
  [(if (apply pred args)
     :PASSED
     :FAILED)
   (last args)])

(defn reread-this-notespace! []
  (actions/reread-notes! *ns*))

(defn eval-this-notespace! []
  (actions/evaluate-notes! *ns*))

(defn realize-note-at-line! [line]
  (actions/realize-note-at-line! *ns* line))

(defmacro D [& forms]
  (cons 'delay forms))


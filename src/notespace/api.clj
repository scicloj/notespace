(ns notespace.api
  (:require [notespace.actions :as actions]
            [notespace.lifecycle :as lifecycle]
            [notespace.context :as ctx]
            [notespace.note :as note]))

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
  `(let [idx# ~note/*notespace-idx*
         ns# *ns*]
     (delay
       (let [result# (do ~@forms)]
         (actions/rerender-note! ns# idx#)
         result#))))

(defmacro F [& forms]
  `(let [idx# ~note/*notespace-idx*
         ns#  *ns*]
     (future
       (let [result# (do ~@forms)]
         (actions/rerender-note! ns# idx#)
         result#))))


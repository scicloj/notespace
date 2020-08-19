(ns notespace.api
  (:require [notespace.note :as note]
            [notespace.lifecycle :as lifecycle]))

(def init lifecycle/init)

(defn check [pred & args]
  [(if (apply pred args)
     :PASSED
     :FAILED)
   (last args)])

(defn reread-this-notespace! []
  (note/reread-notes! *ns*))

;; (defn realize-note-at-line! [line]
;;   (note/reread-notes! *ns*)
;;   (some->> line
;;            (state/ns->line->index *ns*)
;;            (state/ns->note *ns*)
;;            (note/realize-note! *ns*))
;;   [[:realized {:ns   *ns*
;;                :line line}]
;;    #_(render-this-ns!)])

;; (defn realize-this-notespace! []
;;   (note/reread-notes! *ns*)
;;   (->> *ns*
;;        (state/ns->notes)
;;        (run! (partial note/realize-note! *ns*)))
;;   [[:realized {:ns *ns*}]
;;    #_(render-this-ns!)])

(defmacro D [& forms]
  (cons 'delay forms))


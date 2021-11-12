(ns scicloj.notespace.v4.config)


(def *config
  (atom {:messages? true
         :last-eval? true
         :summary? true
         :header? true
         :notes?  true}))

(defn set! [new-config]
  (reset! *config new-config))

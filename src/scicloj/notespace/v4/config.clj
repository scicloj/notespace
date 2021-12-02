(ns scicloj.notespace.v4.config)

(def *config
  (atom {:debug? false
         :last-eval? true
         :summary? true
         :header? true
         :notebook?  true
         :note-layout :vertical}))

(defn set! [new-config]
  (reset! *config new-config))

(defn merge! [new-config]
  (swap! *config merge new-config))

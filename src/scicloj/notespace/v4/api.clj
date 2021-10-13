(ns scicloj.notespace.v4.api
  (:require [scicloj.notespace.v4.kindness] ; <- leave this here
            [scicloj.notespace.v4.state :as v4.state]
            [scicloj.notespace.v4.systems.events :as v4.events-sys]
            [scicloj.notespace.v4.systems.frontend :as v4.frontend-sys]
            [scicloj.notespace.v4.frontend.engine :as v4.frontend.engine]))

(defn restart-events! []
  (v4.state/init!)
  (v4.events-sys/restart!)
  :ok)

(defn restart!
  ([]
   (restart! {}))
  ([frontend-config]
   (restart-events!)
   (v4.frontend-sys/restart! frontend-config)
   :ok))

(defn render-as-html! [target-path]
  (v4.frontend.engine/reset-header! nil)
  (v4.frontend.engine/render-as-html! target-path))

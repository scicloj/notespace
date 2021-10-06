(ns scicloj.notespace.v4.api
  (:require [scicloj.notespace.v4.state :as v4.state]
            [scicloj.notespace.v4.system :as v4.system]
            [scicloj.notespace.v4.frontend.engine :as v4.frontend.engine]))

(defn restart! [config]
  (v4.state/init!)
  (v4.system/restart! config))

(defn render-as-html! [config]
  (v4.frontend.engine/reset-header! nil)
  (v4.frontend.engine/render-as-html! "/tmp/notespace/index.html"))

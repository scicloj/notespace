(ns notespace.scratch
  (:require [notespace.context :as ctx]
            [notespace.events :as events]
            [notespace.renderers.pp :as pp-renderer]))

(comment
  (ctx/mount-renderer pp-renderer/renderer)

  (ctx/unmount-renderer pp-renderer/renderer)

  (ctx/handle
   {:event/type ::events/add-note
    :fx/sync    true
    :note       {:code `(do
                          (Thread/sleep 2000)
                          (+ 1 ~(rand-int 99)))}})

  (ctx/handle
   {:event/type ::events/realize-note
    ;; :fx/sync    true
    :idx        0}))

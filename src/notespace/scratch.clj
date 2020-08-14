(ns notespace.scratch
  (:require [notespace.context :as ctx]
            [notespace.events :as events]
            [notespace.init :as init]
            [notespace.note :as note]))

(->> (note/->ns-topforms-with-metadata *ns*)
     (map note/topform-with-metadata->Note))

^:void ^{:label :intro}
[3]

^:multi
[3]

["a" "b"]

^:multi
["a" "b"]

(comment
  (init/init)

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

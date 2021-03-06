(ns notespace.cli
  (:require [notespace.api :as api]
            [clojure.tools.namespace.find :as ns-find]
            [clojure.java.classpath :as cp]
            [notespace.renderers.gorilla-notes :as gn]
            ))

(defn find-ns-declr [ns-symbol]
  (let  [ns-decls
         (->
          (cp/classpath)
          (ns-find/find-ns-decls)
          )]
    (->>
     ns-decls
     (filter #(= (second %) ns-symbol))
     first)))

(defn eval-and-render-a-notespace [options]

  (let  [ns-symbol (:ns options)]

    (eval (find-ns-declr ns-symbol))
    (in-ns ns-symbol)
    (api/init)
    (api/update-config #(assoc % :evaluation-callback-fn
                               (fn [idx note-count note]
                                 (let [expected-duration (or  (get-in note [:duration]) 0)]
                                   (println "evaluate note: " idx "/" (dec note-count))))))

    (api/eval-and-realize-this-notespace)
    (api/render-static-html)
    )
  )

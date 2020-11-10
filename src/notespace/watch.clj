(ns notespace.watch
  (:require [hawk.core :as hawk]
            [notespace.paths :as paths]
            [clojure.java.io]
            [notespace.source :as source]))

;; Copied from Oz.
;; https://github.com/metasoarous/oz/blob/d187836c72cda23bf5e6544f4e5669db7d77d314/src/clj/oz/live.clj

(defonce watchers (atom {}))

(defn start-watching! [namespace f]
  (when-not (get @watchers namespace)
    (let [watch-path (source/ns->source-filename namespace)]
      ;; Call the function on first watch, so that you don't have to do a no-op save to initialize things
      (f namespace)
      (let [watcher
            (hawk/watch! [{:paths   [watch-path]
                           :handler (fn [context event]
                                     (f namespace))}])]
        (swap! watchers assoc-in [namespace :watcher] watcher)
        ::success!))))

(defn stop-watching!
  [namespace]
  (some-> (get-in @watchers [namespace :watcher])
          (hawk/stop!))
  (swap! watchers dissoc namespace))

(comment
  (start-watching! *ns* println)
  (stop-watching! *ns*))


(ns notespace.watch
  (:require [hawk.core :as hawk]
            [notespace.paths :as paths]
            [clojure.java.io]
            [notespace.source :as source]))

;; Copied from Oz.
;; https://github.com/metasoarous/oz/blob/d187836c72cda23bf5e6544f4e5669db7d77d314/src/clj/oz/live.clj

(defonce watchers (atom {}))

(defn start-watching! [anamespace f]
  (when-not (get @watchers anamespace)
    (let [watch-path (source/ns->source-filename anamespace)]
      ;; Call the function on first watch, so that you don't have to do a no-op save to initialize things
      (f anamespace)
      (let [watcher
            (hawk/watch! [{:paths   [watch-path]
                           :handler (fn [context event]
                                     (f anamespace))}])]
        (swap! watchers assoc-in [anamespace :watcher] watcher)
        ::success!))))

(defn stop-watching!
  [anamespace]
  (some-> (get-in @watchers [anamespace :watcher])
          (hawk/stop!))
  (swap! watchers dissoc anamespace))

(comment
  (start-watching! *ns* println)
  (stop-watching! *ns*))

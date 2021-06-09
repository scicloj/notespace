(ns notespace.v4.watch
  (:require [hawk.core :as hawk]
            [clojure.pprint :as pp]))

(def *path->watcher
  (atom {}))

(defn watch-file [path handler]
  (swap! *path->watcher
         (fn [path->watcher]
           (if (path->watcher path)
             path->watcher
             (assoc path->watcher
                    path
                    (hawk/watch! [{:paths   [path]
                                   :handler handler}]))))))

(defn unwatch-file [path]
  (swap! *path->watcher
         (fn [path->watcher]
           (if-let [handler (path->watcher path)]
             (do (hawk/stop! handler)
                 (dissoc path->watcher path))
             path->watcher))))

(comment
  (watch-file "src/notespace/v4/dummy.clj"
              (fn [ctx e]
                (pp/pprint {:e e
                            :ctx ctx})
                ctx))
  (unwatch-file "src/notespace/v4/dummy.clj"))

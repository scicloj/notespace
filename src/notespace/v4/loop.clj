(ns notespace.v4.loop
  (:require [editscript.core :as editscript]
            [notespace.v4.state :as v4.state]
            [notespace.v4.log :as v4.log]
            [notespace.v4.read :as v4.read]
            [notespace.v4.watch :as v4.watch]
            [notespace.v4.merge :as v4.merge]
            [notespace.v4.note :as v4.note]
            [clojure.pprint :as pp]))

(defn update-notes [file new-notes]
  ;; (v4.log/log-data [::update-notes
  ;;                   {:file file}])
  (swap! v4.state/*state
         update-in [:file->notes file]
         (fn [current-notes]
           (let [merged-notes (v4.merge/merge-notes current-notes
                                                    new-notes)]
             (v4.log/log-data
              ::update-notes
              {:file         file
               :merged-notes merged-notes})
             merged-notes))))

(defn handle-file-update [path]
  (v4.log/log-data [::handle-file-update path])
  (future ; avoid blocking the nREPL thread
    (v4.watch/watch-file
     path
     (fn [_ e]
       (handle-file-update
        (.getPath ^java.io.File (:file e))))))
  (->> path
       slurp
       v4.read/->notes
       (update-notes path)))

(defn handle-eval [{:keys [column line code] :as info}]
  #_(v4.log/log-data
     [::handle-eval
      {:info  info
       :notes (->> code
                   v4.read/->notes)}]))

(comment
  (v4.log/empty-logfile))

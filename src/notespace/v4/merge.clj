(ns notespace.v4.merge
  (:require [notespace.v4.log :as v4.log]
            [editscript.core :as editscript]
            [editscript.edit :as edit]
            [notespace.v4.state :as v4.state]
            [notespace.v4.change :as v4.change]
            [notespace.v4.messages :as v4.messages]
            [notespace.v4.diff :as v4.diff]
            [notespace.v4.note :as v4.note]))

(defn merge-notes [old-notes new-notes]
  ;; (v4.state/add-formatted-message! :DEBUG1
  ;;                                  {:old old-notes
  ;;                                   :new new-notes})
  (v4.diff/diff-by-function (or old-notes [])
                            new-notes
                            (juxt :source :region)))

(defn identify-valid-offsets [current-notes region-notes]
  (let [->read-context            (fn [note]
                                (select-keys note [:region :source]))
        current-read-contexts     (map ->read-context current-notes)
        region-read-contexts      (map ->read-context region-notes)
        current-notes-start-lines (map v4.note/start-line current-notes)
        region-notes-start-lines  (map v4.note/start-line region-notes)
        first-region-line         (first region-notes-start-lines)
        region-n-notes            (count region-notes)]
    (->> current-notes-start-lines
         (map-indexed (fn [idx line]
                        {:idx-offset  idx
                         :line-offset (- line first-region-line)}))
         (filter (fn [{:keys [idx-offset line-offset]}]
                   (->> current-read-contexts
                        (drop idx-offset)
                        (take region-n-notes)
                        (map (fn [note]
                               (v4.note/shift note
                                              (- line-offset))))
                        (= region-read-contexts)))))))

(defn identify-unique-offset [current-notes region-notes]
  (let [valid-offsets (identify-valid-offsets current-notes region-notes)]
    (case (count valid-offsets)
      0 :no-offset-found
      1 (first valid-offsets)
      ;; else
      :nonunique-offset-found)))

(defn merge-eval-region-notes [current-notes region-notes]
  (v4.state/add-formatted-message! :merge-eval-region-notes
                                      {})
  (let [offset-result (identify-unique-offset current-notes
                                              region-notes)]
    (-> (if (keyword? offset-result)
          ;; an failure message
          (do (v4.state/add-formatted-message! offset-result {})
              [])
          ;; else - unique offset found
          (let [{:keys [idx-offset line-offset]}
                offset-result]
            (v4.state/add-formatted-message! :found-eval
                                                {:idx-offset  idx-offset
                                                 :line-offset line-offset})
            (->> region-notes
                 (mapv (fn [i note]
                         [[(+ i idx-offset)]
                          :r
                          (v4.note/shift note line-offset)]))))))))

(defn merge-value [current-notes
                   {:keys [request-id value]
                    :as   event}]
  (-> (if-let [idx (->> current-notes
                        (map-indexed vector)
                        (filter (fn [[_ {:keys [status]}]]
                                  (and (-> status
                                           :state
                                           (= :evaluating))
                                       (-> status
                                           :request-id
                                           (= request-id)))))
                        first
                        first)]
        ;; found where this value belongs:
        [[[idx :status :state] :r :evaluated]
         [[idx :status :value] :r :value]]
        ;; else -- not found:
        [])))

(ns scicloj.notespace.v4.merge
  (:require [scicloj.notespace.v4.log :as v4.log]
            [editscript.core :as editscript]
            [editscript.edit :as edit]
            [scicloj.notespace.v4.state :as v4.state]
            [scicloj.notespace.v4.change :as v4.change]
            [scicloj.notespace.v4.messages :as v4.messages]
            [scicloj.notespace.v4.diff :as v4.diff]
            [scicloj.notespace.v4.note :as v4.note]))

(defn merge-notes [old-notes new-notes]
 (v4.diff/diff-by-function (or old-notes [])
                            new-notes
                            (juxt :source :region)))

(defn ->read-context [note]
  (select-keys note [:region :source]))

(defn start-line [read-context]
  (-> read-context :region first))

(defn shift [read-context line-offset]
  (-> read-context
      (update-in [:region 0] (partial + line-offset))
      (update-in [:region 2] (partial + line-offset))))

(defn identify-valid-offsets [current-notes region-notes]
  (let [current-read-contexts     (map ->read-context current-notes)
        region-read-contexts      (map ->read-context region-notes)
        current-notes-start-lines (map start-line current-notes)
        region-notes-start-lines  (map start-line region-notes)
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
                        (map (fn [read-context]
                               (shift read-context
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
                 (map-indexed (fn [i note]
                                [[(+ i idx-offset)]
                                 :r
                                 (-> note
                                     (shift line-offset)
                                     v4.note/->new-note)]))
                 vec))))))

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
        (let [note-with-value (-> idx
                                  current-notes
                                  (update :status
                                          assoc
                                          :state :evaluated
                                          :value value)
                                  (v4.note/->new-note))]
          [[[idx] :r note-with-value]])
        ;; else -- not found:
        [])))

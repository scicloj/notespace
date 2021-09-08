(ns notespace.v4.merge
  (:require [notespace.v4.log :as v4.log]
            [editscript.core :as editscript]
            [editscript.edit :as edit]
            [notespace.v4.state :as v4.state]
            [notespace.v4.status :as v4.status]))

(defn delete-note [position notes]
  (concat (take position notes)
          (drop (inc position) notes)))

(defn add-note [position new-note notes]
  (concat (take position notes)
          [new-note]
          (drop (inc position) notes)))

(defn replace-note [position new-note notes]
  (-> notes
      vec
      (assoc position new-note)))

(defn apply-edit [edit new-notes notes]
 (->> (let [[[position & _] op & _] edit]
         (case op
           :- (delete-note position
                           notes)
           :+ (add-note position
                        (new-notes position)
                        notes)
           :r (if position
                (replace-note position
                              (new-notes position)
                              notes)
                new-notes)))))

(defn merge-notes [current-notes new-notes]
 (->> [current-notes new-notes]
       (map (fn [notes]
              (mapv #(select-keys % [:source]) notes)))
       (apply editscript/diff)
       edit/get-edits
       (reduce (fn [notes edit]
                 (apply-edit
                  edit
                  new-notes
                  notes))
               current-notes)))


(defn note-digest [note]
  (select-keys note [:region :source]))

(defn note-start-line [note]
  (-> note :region first))

(defn shift-note [note line-offset]
  (-> note
      (update-in [:region 0] (partial + line-offset))
      (update-in [:region 2] (partial + line-offset))))

(defn identify-valid-offsets [current-notes region-notes]
  (let [current-notes-digests     (map note-digest current-notes)
        region-notes-digests      (map note-digest region-notes)
        current-notes-start-lines (map note-start-line current-notes)
        region-notes-start-lines  (map note-start-line region-notes)
        first-region-line         (first region-notes-start-lines)
        region-n-notes            (count region-notes)]
    (->> current-notes-start-lines
         (map-indexed (fn [idx line]
                        {:idx-offset idx
                         :line-offset (- line first-region-line)}))
         (filter (fn [{:keys [idx-offset line-offset]}]
                   (->> current-notes-digests
                        (drop idx-offset)
                        (take region-n-notes)
                        (map (fn [note]
                               (shift-note note
                                           (- line-offset))))
                        (= region-notes-digests)))))))

(defn identify-unique-offset [current-notes region-notes]
  (let [valid-offsets (identify-valid-offsets current-notes region-notes)]
    (case (count valid-offsets)
      0 :no-offset-found
      1 (first valid-offsets)
      ;; else
      :nonunique-offset-found)))

(defn merge-eval-region-notes [current-notes region-notes]
  (v4.status/add :merge-eval-region-notes
                 {})
  (let [offset-result (identify-unique-offset current-notes
                                              region-notes)]
    (if (keyword? offset-result)
      ;; an failure message
      (do (v4.status/add offset-result {})
          current-notes)
      ;; else - unique offset found
      (let [{:keys [idx-offset line-offset]}
            offset-result]
        (v4.status/add :found-eval
                       {:idx-offset  idx-offset
                        :line-offset line-offset})
        (concat (->> current-notes
                     (take idx-offset))
                (->> region-notes
                     (map (fn [note]
                            (shift-note note line-offset))))
                (->> current-notes
                     (drop (+ idx-offset
                              (count region-notes)))))))))


(defn merge-value [current-notes
                   {:keys [request-id value]
                    :as event}]
  (if-let [idx (->> current-notes
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
    (do
      (-> current-notes
          vec
          (update-in [idx :status]
                     assoc
                     :state :evaluated
                     :value value)))
    ;; else -- not found:
    current-notes))

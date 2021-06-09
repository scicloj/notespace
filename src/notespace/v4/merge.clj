(ns notespace.v4.merge
  (:require [notespace.v4.log :as v4.log]
            [editscript.core :as editscript]
            [editscript.edit :as edit]))

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
  ;; (v4.log/log-data [::applying-edit]
  ;;                  {:edit edit
  ;;                   :new-notes new-notes
  ;;                   :notes notes})
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
                new-notes)))
       ;; (v4.log/log-data [::applied-edit edit])
       ))

(defn merge-notes [current-notes new-notes]
  ;; (v4.log/log-data ::merge-notes
  ;;                  {:current-notes current-notes
  ;;                   :new-notes new-notes})
  (->> [current-notes new-notes]
       (map (fn [notes]
              (mapv #(select-keys % [:source]) notes)))
       (apply editscript/diff)
       edit/get-edits
       ;; (v4.log/log-data ::edits)
       (reduce (fn [notes edit]
                 (apply-edit
                  edit
                  new-notes
                  notes))
               current-notes)))



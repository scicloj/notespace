(ns notespace.v4.read
  (:require [clojure.tools.reader]
            [clojure.tools.reader.reader-types]
            [parcera.core :as parcera]))

(defn read-by-tools-reader [code]
  (->> code
       clojure.tools.reader.reader-types/source-logging-push-back-reader
       repeat
       (map #(clojure.tools.reader/read % false ::EOF))
       (take-while (partial not= ::EOF))
       (map (fn [form]
              (let [{:keys [line column
                            end-line end-column
                            source]}
                    (meta form)]
                (when line ; skip forms with no location info
                  {:method :tools-reader
                   :region [line column
                            end-line end-column]
                   :source source}))))
       (filter some?)))

(defn read-by-parcera [code]
  (->> code
       parcera/ast
       rest
       (map (fn [node]
              (let [node-type     (first node)
                    node-contents (rest node)]
                ;; We use parcera only for specific types of
                ;; code blocks, that tools.reader does not
                ;; provide location info for.
                (some-> (cond
                          ;;
                          (#{:number :string :symbol :keyword}
                           node-type)
                          {:source (first node-contents)}
                          ;;
                          (= :comment node-type)
                          {:comment (first node-contents)})
                        (assoc :method :parcera
                               :region
                               (->> node
                                    meta
                                    ((juxt :parcera.core/start
                                           :parcera.core/end))
                                    (mapcat (juxt :row
                                                  (comp inc
                                                        :column)))
                                    vec))))))
       (filter some?)))


(->> "dummy.clj"
     slurp
     ((juxt read-by-tools-reader read-by-parcera))
     (apply concat)
     (group-by :region)
     (map (fn [[region results]]
            (if (-> results count (= 1))
              (first results)
              ;; prefer tools.reader over parcera
              (->> results
                   (filter #(-> % :method (= :tools-reader)))
                   first))))
     (sort-by :region)
     (map #(dissoc % :method))
     (partition-by :comment))




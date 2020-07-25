(ns notespace.check)

(defn ->checks-freqs [notes]
  (when-let [checks-results (->> notes
                                 (map :value)
                                 (filter (fn [v]
                                           (and (vector? v)
                                                (-> v first (#{:FAILED :PASSED})))))
                                 (map first)
                                 seq)]
    (->> checks-results
         frequencies)))

(defn ->checks-summary [checks-freqs]
  (when checks-freqs
    [:div
     "Checks: "
     (->> checks-freqs
          (map (fn [[k n]]
                 [:b {:class (name k)}
                  n " " (name k) " "])))
     [:hr]]))

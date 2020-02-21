(ns notespace.v2.check)

(defn check [pred & args]
  [(if (apply pred args)
     :PASSED
     :FAILED)
   (last args)])

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
                 (let [color (case k
                               :PASSED "green"
                               :FAILED "red")]
                   [:b {:style (str "color:" color)}
                    n " " (name k) " "])))
          (into [:b]))
     [:hr]]))


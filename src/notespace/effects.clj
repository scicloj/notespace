(ns notespace.effects)

(defn realization [{:keys [idx note on-result on-exception]}
                          dispatch!]
  (try
    (dispatch! (assoc on-result
                      :idx idx
                      :value (-> note :code eval)))
    (catch Exception e
      (dispatch! (assoc on-exception
                        :exception e)))))

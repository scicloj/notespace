(ns notespace.v2.vega-tst
  (:require [notespace.v2.vega :as sut]
            [clojure.test :refer :all]))


  (defn play-data [& names]
    (for [n names
          i (range 20)]
      {:time i :item n :quantity (+ (Math/pow (* i (count n)) 0.8) (rand-int (count n)))}))

  (def line-plot
    {:width 300
     :height 300
     :data
     {:values (play-data "monkey" "slipper" "broom")}
     :encoding {:x {:field "time" :type "quantitative"}
                :y {:field "quantity" :type "quantitative"}
                :color {:field "item" :type "nominal"}}
     :mark "line"})      

(deftest to-hiccpu
  (testing ""
    (is (= :svg
           (first
            (sut/vega->hiccup line-plot :vega-lite) )))

      )
  )





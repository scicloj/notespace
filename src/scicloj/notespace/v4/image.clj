(ns scicloj.notespace.v4.image
  (:require [tech.v3.resource :as resource]
            [clojure.java.io :as io]
            [babashka.fs :as fs]
            [scicloj.tempfiles.api :as tempfiles])
  (:import java.io.File
           java.awt.image.BufferedImage
           javax.imageio.ImageIO))

(defn buffered-image->hiccup
  ([^BufferedImage buffered-image]
   (buffered-image->hiccup buffered-image
                           {}))
  ([^BufferedImage buffered-image options]
   (let [{:keys [route path]} (tempfiles/tempfile! ".png")]
     (ImageIO/write buffered-image
                    "png"
                    ^java.io.File (io/file path))
     [:img (merge {:src route}
                  options)])))




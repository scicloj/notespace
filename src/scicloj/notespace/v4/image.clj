(ns scicloj.notespace.v4.image
  (:require [tech.v3.resource :as resource]
            [clojure.java.io :as io]
            [babashka.fs :as fs])
  (:import java.io.File
           java.awt.image.BufferedImage
           javax.imageio.ImageIO))

(defn buffered-image->hiccup
  ([^BufferedImage buffered-image]
   (buffered-image->hiccup buffered-image
                           {}))
  ([^BufferedImage buffered-image options]
   (when-not (fs/exists? "resources/public/notespace-files/")
     (fs/create-dirs "resources/public/notespace-files/"))
   (let [filename (str (rand-int 9999999) ".png")
         path     (str "resources/public/notespace-files/" filename)
         file     ^java.io.File (io/file path)
         hiccup   [:img (merge {:src (str "notespace-files/" filename)}
                               options)]]
     (resource/track hiccup
                     {:track-type :gc
                      :dispose-fn #(.delete file)})
     (ImageIO/write buffered-image
                    "png"
                    file)
     hiccup)))

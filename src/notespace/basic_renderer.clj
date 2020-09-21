(ns notespace.basic-renderer
  (:require [notespace.paths :as paths]
            [clojure.java.io :refer [resource]]
            [hiccup.page :as page]
            [cambium.core :as log]
            [notespace.view :as view]
            [notespace.note :as note]
            [notespace.js :as js]
            [notespace.css :as css]
            [notespace.state :as state]
            [notespace.cdn :as cdn])
  (:import java.io.File))

(defn copy-waiting-gif! []
  (paths/copy-to-ns-target-path (resource "images/Ball-1s-24px.gif")
                                "waiting.gif"))

(defn render-to-file! [render-fn path]
  (let [path-to-use (or path (str (File/createTempFile "rendered" ".html")))
        html (page/html5 (render-fn))]
    (copy-waiting-gif!)
    (spit path-to-use html)
    (log/info [::wrote path-to-use])
    html))

(defn notes->hiccup [namespace notes]
  (->> notes
       (map (partial note/note->note-state namespace))
       (view/notes-and-states->hiccup namespace notes)))

(defn render-notes! [namespace notes & {:keys [file]}]
  (render-to-file! (partial notes->hiccup namespace notes)
                   file))

(defn render-ns [namespace]
  (hiccup.core/html
   [:html
    (into [:head
           (js/mirador-setup)
           (css/include-css (state/config [:css]))]
          (mapcat cdn/header [:prettify :datatables :fonts]))
    [:body
     (if (not namespace)
       "Waiting for a first notespace to appear ..."
       (do (note/read-notes-seq! namespace)
           (notes->hiccup
            namespace
            (state/ns->notes namespace))))]]))


(defn render-ns! [namespace]
  (render-to-file! (partial render-ns namespace)
                   (paths/ns->out-filename namespace))
  (state/assoc-in-state! [:last-ns-rendered] namespace)
  [:rendered {:ns namespace}])

(defn render-this-ns []
  (render-ns *ns*))

(defn render-this-ns! []
  (log/info ::render-this-ns!)
  (render-ns! *ns*))

(defn effect! [[change-type paths-and-_]]
  (log/info {:change-type change-type
             :paths (mapv first paths-and-_)
             :state-size (count @state/state)})
  (when (and (= change-type :update)
             (->> paths-and-_
                  (partition 2)
                  (map (comp first first))
                  (= [:ns->note-states])))
    (render-this-ns!)))


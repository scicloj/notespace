(ns notespace.v2.live-reload
  (:require [org.httpkit.server :as httpkit-server]
            [compojure.core :refer (GET POST ANY PUT DELETE defroutes)]
            [compojure.handler :as handler]
            [ring.middleware.session]
            [ring.middleware.reload]
            [compojure.route :as route]
            [com.akolov.mirador.core :refer [watch-reload watcher-folder]]
            [cambium.core :as log]
            [clojure.java.browse :as browse]
            [notespace.v2.note :as note]
            [notespace.v2.config :as config]))

(defn html-response [body]
      {:status  200
       :headers {"Content-Type" "text/html; charset utf-8"}
       :body    body})

(defn file-exists? [path]
  (.exists  (java.io.File. path)))

(defn slurp-when-exists [path]
  (when (file-exists? path)
    (slurp path)))

(defn notespace-html []
  (or (some-> @note/last-ns-rendered
              note/ns->out-filename
              slurp)
      (note/render-ns nil)))

(defn main [req]
  (->> (notespace-html)
       html-response))

(defroutes routes
  (GET "/" req (main req))
  (route/resources "/static" {:root "static"}))

(def app (-> routes
             (ring.middleware.reload/wrap-reload)
             (watch-reload {:watcher (watcher-folder "resources")
                            :uri     "/watch-reload"})
             handler/site))

(defonce server (atom nil))

(defn stop! []
  (when-not (nil? @server)
    ;; graceful shutdown: wait 100ms for existing requests to be finished
    ;; :timeout is optional, when no timeout, stop immediately
    (@server :timeout 100)
    (reset! server nil)))

(defn port []
  (:live-reload-port @config/defaults))

(defn start! []
  (let [p (port)]
    (println "Server starting...")
    (reset! server
            (try (httpkit-server/run-server
                  #'app
                  {:port p
                   :join? false})
                 (catch Exception e
                   (log/error [::live-reload-server-start-failed e]))))
    (println "Live-reload server ready at port" p ".")))


(defn restart! [& args]
  (stop!)
  (apply start! args))

(defn open-browser []
  (->> (port)
       (str "http://localhost:")
       browse/browse-url
       future))


;; automatic init

(restart!)

(defonce opened (open-browser))

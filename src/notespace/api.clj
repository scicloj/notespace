(ns notespace.api
  (:require [notespace.actions :as actions]
            [notespace.note :as note]
            [notespace.state :as state]
            [notespace.util :as u]
            [notespace.paths :as paths]
            [notespace.watch :as watch]
            [gorilla-notes.core :as gn]
            [notespace.source :as source]
            [notespace.view :as view]
            [notespace.events :as events]
            [notespace.context :as ctx]
            [notespace.defaults :as defaults]
            [notespace.renderers.gorilla-notes :as renderers-gn]
            [babashka.fs :as fs]))

(defn update-config [f]
  (actions/update-config! f))

(def toggle-single-note-mode
  actions/toggle-single-note-mode!)

(comment
  (toggle-single-note-mode true))

(defn init [& {:keys [open-browser? port]
               :or   {open-browser? false}}]
  (ctx/handle
   {:event/type    ::events/reset-but-keep-config
    :fx/sync       true
    :initial-state defaults/initial-state})
  (if port
    (renderers-gn/init {:port port})
    (renderers-gn/init))
  (when open-browser?
    (renderers-gn/browse))
  (ctx/unmount-renderer #'renderers-gn/renderer)
  (ctx/mount-renderer #'renderers-gn/renderer)
  (paths/clean-dynamic-files-dir)
  :ok)

(defn init-with-browser [& options]
  (apply init :open-browser? true options))

(defn stop-server []
  (renderers-gn/stop-server!))

(defmacro view [form]
  `(do
     (gn/reset-notes!)
     (-> {:value    ~(eval form)
          :metadata {:source ""}
          :kind     ~(note/topform-with-metadata->kind form)}
         view/note->hiccup
         gn/add-note!)
     :ok))

(defn reread-this-notespace []
  (actions/reread-notes! *ns*))

(defn eval-this-notespace []
  (actions/act-on-notes! *ns* [actions/eval-note!]))

(defn eval-and-realize-this-notespace []
  (actions/act-on-notes! *ns* [actions/eval-note!
                               actions/realize-note!]))

(defn eval-note-at-line [line]
  (actions/act-on-note-at-line! *ns* line [actions/eval-note!]))

(defn realize-note-at-line [line]
  (actions/act-on-note-at-line! *ns* line [actions/realize-note!]))

(defn eval-and-realize-note-at-line [line]
  (actions/act-on-note-at-line! *ns* line [actions/eval-note!
                                           actions/realize-note!]))

(defn eval-and-realize-notes-from-line [line]
  (actions/act-on-notes-from-line! *ns* line [actions/eval-note!
                                              actions/realize-note!]))
(defn eval-and-realize-note-at-change
  ([]
   (eval-and-realize-note-at-change *ns*))
  ([anamespace]
   (actions/eval-and-realize-note-at-change! anamespace)))

(defn eval-and-realize-notes-from-change
  ([]
   (eval-and-realize-notes-from-change *ns*))
  ([anamespace]
   (actions/eval-and-realize-notes-from-change! anamespace)))

(defonce namespaces-listening-to-changes
  (atom #{}))

(defn listen
  ([]
   (listen *ns*))
  ([anamespace]
   (when-not (state/single-note-mode?)
     (actions/act-on-notes! anamespace
                            [actions/eval-note!
                             actions/realize-note!]))
   (swap! namespaces-listening-to-changes conj anamespace)))

(defn unlisten
  ([]
   (unlisten *ns*))
  ([anamespace]
   (swap! namespaces-listening-to-changes #(remove #{anamespace} %))))

(defonce listen-sleep
  (atom 0))

(defonce periodically-react-to-changes
  (future
    (while true
      (Thread/sleep @listen-sleep)
      (doseq [anamespace @namespaces-listening-to-changes]
        (if (state/single-note-mode?)
          (eval-and-realize-note-at-change anamespace)
          (eval-and-realize-notes-from-change anamespace))))))

(defn render-static-html
  ([]
   (render-static-html nil))
  ([path]
   (let [path-to-use (or path (paths/ns->target-path *ns*))]
     (gn/render-current-state! path-to-use)
     (let [files-dirname (format "%s/%s" (fs/parent path-to-use)
                                 paths/files-dirname)]
       (when-not (fs/exists? files-dirname)
         (fs/create-dirs files-dirname))
       (fs/copy-tree paths/dynamic-files-dirname
                     files-dirname
                     {:replace-existing true}))
     (println [:rendered path-to-use]))))

(defmacro R [symbols & forms]
  `(reify clojure.lang.IDeref
     (deref [~'_]
       (when-let [inputs# (state/sub-get-in :inputs)]
         (let [{:keys [~@symbols]} inputs#]
           ~@forms)))))

(defn tests-summary
  ([]
   (tests-summary *ns*))
  ([anamespace]
   (tests-summary anamespace nil))
  ([anamespace note-kinds-set]
   (-> (state/sub-get-in :ns->notes anamespace)
       (view/notes->tests-summary note-kinds-set)
       (vary-meta
        assoc
        :notespace.kind :notespace.kinds/hiccup))))

(defn clojure-tests-summary
  ([]
   (clojure-tests-summary *ns*))
  ([anamespace]
   (tests-summary anamespace {:note-kinds-set
                              #{notespace.kinds/clojure-test}})))

(defn midje-summary
  ([]
   (midje-summary *ns*))
  ([anamespace]
   (tests-summary anamespace {:note-kinds-set
                              #{notespace.kinds/midje}})))


(defn file-target-path
  [filename]
  (when-not (fs/exists? paths/dynamic-files-dirname)
    (fs/create-dirs paths/dynamic-files-dirname))
  (format "%s/%s"
          paths/dynamic-files-dirname
          filename))

(defn file-link-tag [title filename]
  ^{:notespace.kind :notespace.kinds/hiccup}
  [:a
   {:href (paths/file-path-for-url filename)}
   title])

(defn img-file-tag [filename options]
  ^{:notespace.kind :notespace.kinds/hiccup}
  [:img
   (merge {:src (paths/file-path-for-url filename)}
          options)])

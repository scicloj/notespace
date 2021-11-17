(defproject scicloj/notespace "4-alpha-13"
  :description "Notebook experience in your Clojure namespace."
  :url "http://github.com/scicloj/notespace"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :plugins [[lein-tools-deps "0.4.5"]]
  :middleware [lein-tools-deps.plugin/resolve-dependencies-with-deps-edn]
  :lein-tools-deps/config {:config-files [:install :user :project]}
  :profiles {:dev {:cloverage    {:runner :midje}
                   :dependencies [[midje "1.9.9"]
                                  [aerial.hanami "0.12.7"]
                                  [scicloj/tablecloth "6.025"]]
                   :plugins      [[lein-midje "3.2.1"]
                                  [lein-cloverage "1.1.2"]]
                   :repl-options {:nrepl-middleware [scicloj.notespace.v4.nrepl/middleware]}}}
  :resource-paths ["src" "resources" "target/webly"])



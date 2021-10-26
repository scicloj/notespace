(defproject scicloj/notespace "4-alpha-8"
  :description "Notebook experience in your Clojure namespace."
  :url "http://github.com/scicloj/notespace"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [metosin/jsonista "0.2.5"]
                 [org.clojure/tools.reader "1.3.4"]
                 [org.clojure/core.async "1.1.587"]
                 [org.clojure/core.cache "1.0.207"]
                 [babashka/fs "0.0.1"]
                 [org.clojure/tools.namespace "1.1.0"]
                 [carocad/parcera "0.11.6"]
                 [org.antlr/antlr4-runtime "4.7.1"]
                 [juji/editscript "0.5.7"]
                 [hawk "0.2.11"]
                 [com.nextjournal/beholder "1.0.0"]
                 [techascent/tech.resource "5.04"]
                 [integrant "0.8.0"]
                 [org.scicloj/tempfiles "1-alpha2"]
                 [org.scicloj/kindly "1-alpha2"]
                 ;; avoid conflict with jsonista
                 [scicloj/gorilla-notes "0.5.15"
                  :exclusions [com.fasterxml.jackson.core/jackson-core
                               com.fasterxml.jackson.dataformat/jackson-dataformat-cbor
                               com.fasterxml.jackson.dataformat/jackson-dataformat-smile
                               com.fzakaria/slf4j-timbre]]
                 [com.fasterxml.jackson.core/jackson-core "2.10.0"]
                 [com.fasterxml.jackson.core/jackson-annotations "2.10.0"]
                 [com.fasterxml.jackson.dataformat/jackson-dataformat-cbor "2.10.0"]
                 [com.fasterxml.jackson.dataformat/jackson-dataformat-smile "2.10.0"]]
  :profiles {:dev {:cloverage    {:runner :midje}
                   :dependencies [[midje "1.9.9"]
                                  [aerial.hanami "0.12.7"]]
                   :plugins      [[lein-midje "3.2.1"]
                                  [lein-cloverage "1.1.2"]]
                   :repl-options {:nrepl-middleware [scicloj.notespace.v4.nrepl/middleware]}}})


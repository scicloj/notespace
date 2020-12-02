(defproject scicloj/notespace "3-alpha3-SNAPSHOT"
  :description "Notebook experience in your Clojure namespace."
  :url "http://github.com/scicloj/notespace"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [differ "0.3.3"]
                 [hiccup "1.0.5"]
                 [zprint "0.5.2"]
                 [markdown-clj "1.10.0"]
                 [com.rpl/specter "1.1.3"]
                 [metosin/jsonista "0.2.5" ]
                 [garden "1.3.9"]
                 [cambium/cambium.core         "0.9.3"]
                 [cambium/cambium.codec-simple "0.9.3"]
                 [cambium/cambium.logback.core "0.4.3"]
                 [org.clojure/tools.reader "1.3.2"]
                 [ring/ring "1.8.0"]
                 [compojure "1.6.1"]
                 [http-kit "2.4.0-alpha6"]
                 [com.akolov/mirador "0.2.1"]
                 [javax.servlet/servlet-api "2.5"]
                 [hickory "0.7.1"]
                 [metasoarous/darkstar "0.1.0"]
                 [org.clojure/core.async "1.1.587"]
                 [cljfx "1.7.5"]
                 [org.clojure/core.cache "1.0.207"]
                 [hawk "0.2.11"]


                 ;; avoid conflict with jsonista
                 [scicloj/gorilla-notes "0.5.0-SNAPSHOT"
                  :exclusions [com.fasterxml.jackson.core/jackson-core
                               com.fasterxml.jackson.dataformat/jackson-dataformat-cbor
                               com.fasterxml.jackson.dataformat/jackson-dataformat-smile]]
                 [com.fasterxml.jackson.core/jackson-core "2.10.0"]
                 [com.fasterxml.jackson.core/jackson-annotations "2.10.0"]
                 [com.fasterxml.jackson.dataformat/jackson-dataformat-cbor "2.10.0"]
                 [com.fasterxml.jackson.dataformat/jackson-dataformat-smile "2.10.0"]

                 ]
  :profiles {:dev {
                   :cloverage {:runner :midje}
                   :dependencies [[midje "1.9.9"]]
                   :plugins [[lein-midje "3.2.1"]
                             [lein-cloverage "1.1.2"]]}})

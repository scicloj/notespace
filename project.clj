(defproject notespace "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [differ "0.3.3"]
                 [hiccup "1.0.5"]
                 [metasoarous/oz "1.6.0-alpha5"]
                 [clojuress "0.1.0-SNAPSHOT"]
                 [alembic "0.3.2"]
                 [zprint "0.5.2"]
                 [cljfmt "0.6.4"]
                 [markdown-clj "1.10.0"]]
  :repl-options {:init-ns notespace.core})

(defproject thought_stream "0.1.0-SNAPSHOT"
  :description "Simple app for connecting thoughts together into a stream."
  :url "https://thought-stream.io/"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [ring "1.7.1"]
                 [com.h2database/h2 "1.4.197"]
                 [org.clojure/java.jdbc "0.7.8"]
                 [hiccup "1.0.5"]
                 [compojure "1.6.1"]
                 [org.jsoup/jsoup "1.11.3"]
                 [org.clojure/test.check "0.9.0"]]
  :main ^:skip-aot thought-stream.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})

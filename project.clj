(defproject blog-zone "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [
    [org.clojure/clojure "1.6.0"]
    [compojure "1.1.8"]
		[org.clojure/java.jdbc "0.3.5"]
    [java-jdbc/dsl "0.1.0"]
		[mysql/mysql-connector-java "5.1.25"]
    [com.cemerick/friend "0.2.1"]
    ;; v pls remove v
		[ring-basic-authentication "1.0.5"]]
  :plugins [[lein-ring "0.8.11"]]
  :ring {:handler blog-zone.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})

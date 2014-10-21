(ns blog-zone.auth
	(:require [clojure.java.jdbc :as jdbc]
		[java-jdbc.sql :as sql]))

(def database {
	:subprotocol "mysql"
	:subname "//localhost:3306/blog"
	:user "app_user"
	:password "STR0ngpassw5rd"
	:zeroDateTimeBehavior "convertToNull"})

(defn authenticated? [uname passwd]
	(let [cont (:cont (first (jdbc/query database ["select count(*) as cont from users where username=? and password=password(?)" uname passwd])))]
	(= 1 cont)))
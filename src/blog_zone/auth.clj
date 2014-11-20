(ns blog-zone.auth
	(:require [clojure.java.jdbc :as jdbc]
		[java-jdbc.sql :as sql]))

(def database {
	:subprotocol "mysql"
	:subname "//localhost:3306/blog2"
	:user "app_user"
	:password "STR0ngpassw5rd"
	:zeroDateTimeBehavior "convertToNull"})

(defn authenticated? [uname passwd]
	(do (print (str "authenticating user " uname " using password " passwd))
	(let [cont (:cont (first (jdbc/query database ["select count(*) as cont from users where username=? and password=password(?) and user_id in (select user_id from admins);" uname passwd])))]
	(= 1 cont))))
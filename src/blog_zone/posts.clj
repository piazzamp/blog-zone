(ns blog-zone.posts
	(:require [clojure.java.jdbc :as jdbc]
		[java-jdbc.sql :as sql]))

(def now (str (java.sql.Timestamp. (System/currentTimeMillis))))

(def database {
	:subprotocol "mysql"
	:subname "//localhost:3306/blog"
	:user "app_user"
	:password "STR0ngpassw5rd"
	:zeroDateTimeBehavior "convertToNull"})

(defn maxmin-id "grabs the max or min id in a table. takes :max / :min and table name (string) arguments." 
	[type table]
	(if (#{"posts" "comments"} table) ;; check input against whitelist of tables
		(cond 
			(= type :max) (if-let [id (:id (first (jdbc/query database [(str "select max(id) as id from " table)])))] id 0)
			(= type :min) (if-let [id (:id (first (jdbc/query database [(str "select min(id) as id from " table)])))] id 0))))

(defn all "grabs all posts from posts table with the most recent posts first" 
	[] (jdbc/query database (sql/select * :posts (sql/order-by {:updated_date :desc}))))

(defn get-post "returns a map of the post when passed a valid id"
	[id] (first (jdbc/query database
		["select u.username as username, p.title as title, p.body as body, p.updated_date as updated_date, p.created_date as created_date
		from posts p left join users u on u.user_id = p.author where p.id = ?" id])))

(defn next-post-id [id] (:id (first (jdbc/query database ["select min(id) as id from posts where id > ?" id]))))

(defn prev-post-id [id] (:id (first (jdbc/query database ["select max(id) as id from posts where id < ?" id]))))

(defn create "takes a post map and inserts it into the posts table"
	[params]
	(jdbc/insert! database :posts (merge params {:created_date now :updated_date now :id (inc (maxmin-id :max "posts"))})))

(defn delete "deletes a post from the posts table when give a valid id"
	[id] (jdbc/delete! database :posts (sql/where {:id id})))
;; (jdbc/delete! <db var> <table name> (sql/where {:column value}))

(defn save "updates a row in the posts table based on an id and new post map"
	[id params] 
	(jdbc/update!   :posts (merge {:updated_date now} params) (sql/where {:id id})))

(defn get-comments [post-id]
	(jdbc/query database (sql/select * :comments (sql/where {:post_id post-id}) (sql/order-by {:created_date :asc}))))

(defn get-userid "get the user id for a username or get a new user id if that user does not yet exist"
	;;will probably have to change after some authentication is in place, friend
	[uname]
	(if-let [uid (:user_id (first (jdbc/query database (sql/select :user_id :comments (sql/where {:username uname})))))] 
		uid ;; then
		(inc (:user_id (first (jdbc/query database ["select max(user_id) as user_id from comments"]))))))

(defn save-comment [post-id coment]
	;;check for required fields here?
	(jdbc/insert! database :comments (merge {:post_id post-id :created_date now :updated_date now :id (inc (maxmin-id :max "comments")) :user_id (get-userid (:username coment))} coment)))


(defn get-user [user-id]
	(first (jdbc/query database ["select username, join_date from users where user_id = ?" user-id])))

(defn get-addr [user-id]
	(first (jdbc/query database ["select street_1, street_2, city, state, zip  from addresses where user_id = ?" user-id])))

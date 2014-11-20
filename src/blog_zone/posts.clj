(ns blog-zone.posts
	(:require [clojure.java.jdbc :as jdbc]
		[java-jdbc.sql :as sql]
		[clojure.tools.logging :as log]))

(def now (str (java.sql.Timestamp. (System/currentTimeMillis))))

(def database {
	:subprotocol "mysql"
	:subname "//localhost:3306/blog2"
	:user "app_user"
	:password "STR0ngpassw5rd"
	:zeroDateTimeBehavior "convertToNull"})

(defn maxmin-id "grabs the max or min id in a table. takes :max / :min and table name (string) arguments." 
	[type table]
	(if (#{"posts" "comments"} table) ;; check input against whitelist of tables
		(cond 
			(and (= type :max)(= (clojure.string/lower-case table) "posts")) (if-let [id (:id (first (jdbc/query database ["select max_post_id() as id"])))] id 0)
			(= type :max) (if-let [id (:id (first (jdbc/query database [(str "select max(id) as id from " table)])))] id 0)
			(= type :min) (if-let [id (:id (first (jdbc/query database [(str "select min(id) as id from " table)])))] id 0))))

(defn all "grabs all posts from posts table with the most recent posts first" 
	[] (jdbc/query database (sql/select * :posts (sql/order-by {:updated_date :desc}))))

(defn top-posts "grabs all posts from the top_posts view which has an entry threshold of 3 likes"
	[] (jdbc/query database (sql/select * :top_posts (sql/order-by {:updated_date :desc}))))

(defn get-post "returns a map of the post when passed a valid id"
	[id] (first (jdbc/query database
		["select u.username as username, p.title as title, p.body as body, p.updated_date as updated_date, p.created_date as created_date,
		p.author as user_id from posts p left join users u on u.user_id = p.author where p.id = ?" id])))

(defn get-posts-by-user "get all the posts by one user"
	[id]
	(log/info (sql/select * :posts (sql/where {:author id}) (sql/order-by {:updated_date :desc})))
	(jdbc/query database (sql/select * :posts (sql/where {:author id}) (sql/order-by {:updated_date :desc}))))

(defn next-post-id [id] (:id (first (jdbc/query database ["select min(id) as id from posts where id > ?" id]))))

(defn prev-post-id [id] (:id (first (jdbc/query database ["select max(id) as id from posts where id < ?" id]))))

(defn create "takes a post map and inserts it into the posts table"
	[params]
	;;(jdbc/insert! database :posts (merge params {:created_date now :updated_date now :id (inc (maxmin-id :max "posts"))}))
	(log/info (str "calling procedure... " params))
	(jdbc/db-do-prepared database "call save_post(?, ?, ?);" [(params :title),(params :body),(params :author)]))

(defn delete "deletes a post from the posts table when give a valid id"
	[id] (jdbc/delete! database :posts (sql/where {:id id})))
;; (jdbc/delete! <db var> <table name> (sql/where {:column value}))

(defn save "updates a row in the posts table based on an id and new post map"
	[id params] 
	(jdbc/update! database :posts (merge {:updated_date now} params) (sql/where {:id id})))

(defn get-comments [post-id]
	(jdbc/query database 
		["select u.username as username, c.body as body, c.updated_date as updated_date from comments c
		join users u on u.user_id=c.user_id where c.post_id=? order by created_date asc" post-id]))

(defn get-comments-by-user "retrieve all of a user's comments"
	[id] 
	(jdbc/query database (sql/select * :comments (sql/where {:user_id id}))))

(defn get-userid "get the user id for a username or get a new user id if that user does not yet exist"
	;;will probably have to change after some authentication is in place, friend
	[uname]
	(if-let [uid (:user_id (first (jdbc/query database (sql/select :user_id :users (sql/where {:username uname})))))] 
		uid ;; then
		(do (jdbc/insert! database :users {:username uname :user_id 1}) (get-userid uname))))

(defn like-post "add a like to a post using the name in the auth header and the passed post-id"
	[id headers]
	(log/info (str headers))
	(log/info (str (headers :authorization)))
	(jdbc/insert! database :post_likes {:post_id id, :user_id (headers :authorization)}))

(defn get-likes "get vector of users who like a post"
	[id]
	(jdbc/query database (sql/select :user_id :post_likes (sql/where {:post_id id}))))

(defn save-comment [post-id coment]
	;;check for required fields here?
	(log/info "comment: " coment)
	(jdbc/insert! database :comments {:post_id post-id :created_date now :updated_date now :id 0 :user_id (get-userid (coment :username)) :body (coment :body)}))

(defn delete-comment [comment-id]
	(jdbc/delete! database :comments (sql/where {:id comment-id})))

(defn get-user [user-id]
	(first (jdbc/query database ["select username, join_date from users where user_id = ?" user-id])))

(defn get-addr [user-id]
	(first (jdbc/query database ["select street_1, street_2, city, state, zip  from addresses where user_id = ?" user-id])))

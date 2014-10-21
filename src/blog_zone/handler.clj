(ns blog-zone.handler
  (:require 
		[compojure.core :refer :all]
		[compojure.handler :as handler]
		[compojure.route :as route]
		[ring.util.response :as response]
		[ring.middleware.basic-authentication :as auth]
		[blog-zone.views :as views]
		[blog-zone.posts :as posts]
		[blog-zone.auth :as bauth]))

;(defn authenticated? [uname passwd] (and (= uname "username")(= passwd "a g00d password")))

(defroutes public-routes
	(GET "/" [] (views/home))
	(GET "/:id" [id] (views/view-post id))
	(GET "/:id/comment" [id] (views/add-comment id))
	(POST "/:id" [id & coment] (do (posts/save-comment id coment) (response/redirect (str "/" id "#comments"))))
	(GET "/users/:id" [id] (views/view-user id))
	(route/resources "/"))

(defroutes protected-routes
	(GET "/admin" [] (views/admin-blog-page))
	(GET "/admin/add" [] (views/add-post))
	(GET "/admin/:id/delete" [id] 
		(do (posts/delete id) (response/redirect "/admin")))
	(GET "/admin/:id/edit" [id] (views/edit-post id))
	(POST "/admin/create" [& params] 
		(do (posts/create params) (response/redirect "/admin")))
	(POST "/admin/:id/save" [id & params]
		(do (posts/save id params) (response/redirect "/admin"))))

(defroutes app-routes
	public-routes
	(auth/wrap-basic-authentication protected-routes bauth/authenticated?)
	(route/not-found "Not Found"))

(def app
  (handler/site app-routes))
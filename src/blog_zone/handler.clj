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
	(context "/:id" [id]
		(GET "/" [] (views/view-post id))
		(GET "/like" request (do (posts/like-post id (request :headers)) (response/redirect "/")))
		;(GET "/like" request (request :headers)))
		(GET "/comment" [] (views/add-comment id))
		(POST "/" [& coment] (do (posts/save-comment id coment) (response/redirect (str "/" id "#comments"))))
	) ;; close ':id' context
	(GET "/u/:id" [id] (views/view-user id))
	(route/resources "/"))

(defroutes protected-routes
	(context "/admin" []
		(GET "/" [] (views/admin-blog-page))
		(GET "/add" [] (views/add-post))
		(context "/:pid" [pid] 
			(GET "/delete" [pid]
				(do (posts/delete pid) (response/redirect "/admin")))
			(GET "/edit" [pid] (views/edit-post pid))
			(GET "/comment/:cid/delete" [cid] 
				(do (posts/delete-comment cid) (response/redirect (str "/admin/" pid "/edit"))))
			(POST "/save" [& params]
				(do (posts/save pid params) (response/redirect "/admin"))))
		(POST "/create" [& params] 
			(do (posts/create params) (response/redirect "/admin")))
	))

(defroutes app-routes
	public-routes
	(auth/wrap-basic-authentication protected-routes bauth/authenticated?)
	(route/not-found "Not Found"))

(def app
  (handler/site app-routes))
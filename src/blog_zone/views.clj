(ns blog-zone.views
	(:require 
		[hiccup.core :as hiccup]
		[hiccup.form :as form]
		[hiccup.page :as page]
		[blog-zone.posts :as posts]))

(def admin-url "/admin")
(def blog-name "one cool blog")

(defn layout [title & content] ;;content should be hiccup-style vectors
	(hiccup/html [:head [:title title] 
						(page/include-css "/blog-zone.css")
						(page/include-css "//maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap.min.css")] 
					 [:body content]))

(defn user-post-summary [post]
	(let [{id :id title :title body :body created_date :created_date} post]
;;TO-DO: figure out how to easily destructure maps and bind keywords to symbols
	[:section
		[:section.post-heading [:h3.title [:a {:href (str "/" id)} (hiccup/h title)]] [:p.date (hiccup/h created_date)]]
		[:section (hiccup/h body)] [:hr]]))

(defn display-comment [{:keys [post_id id body updated_date username] :as coment}]
	[:section.comments [:h5.title (hiccup/h username)] [:p.date updated_date]
	[:p (hiccup/h body)]])

(defn admin-display-comment [{:keys [post_id id body updated_date username] :as coment}]
	 [:section (display-comment coment)
	 [:a {:href (str "comment/" id "/delete") :style "padding-left:3%;"} "kill"]])

(defn home [] 
	(print "accessing home")
	(layout blog-name
	[:h1 blog-name]
	(map user-post-summary (posts/all))))
;; add a footer, maybe peek at RPMS for hints

(defn admin-post-summary [post]
	(let [{id :id title :title body :body created_date :created_date} post]
			[:section [:hr]
			[:div [:h4.title {:title (hiccup/h created_date)} (hiccup/h title)] 
				[:p.date (hiccup/h created_date)]
			[:section (hiccup/h body)]
			[:section.actions 
				[:a {:href (str admin-url "/" id "/edit")} "edit"] " / "
				[:a {:href (str admin-url "/" id "/delete")} "delete"]]]]))

(defn admin-blog-page []
        (layout "admin"
                [:h1 (hiccup/h "<admin>")]
                [:h2 "all posts – " [:a {:href (str admin-url "/add")} "add another"]]
                (map admin-post-summary (posts/all))))

(defn add-post []
	(layout "One Cool Blog | add post"
		(list [:h2 "add post"]
			(form/form-to [:post (str admin-url "/create")]
				(form/text-field "title" "title") [:br][:br]
				(form/text-area {:rows 8 :class "input"} "body" "body") [:br]
				(form/submit-button {:class "btn btn-defualt"} "shoot"))
				;;abort button doesn't abort!
				[:a {:href admin-url} [:button {:style "color:inherit;" :class "btn btn-default"} "abort"]])))

(defn edit-post [id]
	(let [post (posts/get-post id)]
		(layout (str blog-name " | edit post")
			 (list [:h2 (post :title)]
			 		(form/form-to [:post (str admin-url "/" id "/save")]
			 		(form/text-field {:class "input"} "title" (post :title)) [:br]
					[:br]
					(form/text-area {:rows 8 :class "input"} "body" (post :body)) [:br]
					[:button {:type "submit" :class "btn btn-defualt"} "save"])
			 [:a {:href admin-url :style "text-decoration:none;" :class "btn btn-default"} "cancel"])
			 [:hr][:h3 "trolls in teh dungeon"]
			 (map admin-display-comment (posts/get-comments id)))))
;; TO-DO: admin should be able to delete comments from this view?


(defn view-post [id]
	(if-let [{:keys [title body updated_date username]} (posts/get-post id)]
		(layout (str blog-name " | " title)
			[:h1 (str blog-name "  |  " title " by " username)]
			[:p [:b username]]
			[:p {:style "font-family:monospace;"} updated_date] 
			[:a.nav {:href "/"} "home"] 
;; consider using glyphicons or someting similar for this nav section
			(let [id-num (Integer/parseInt id)]
				[:span
				(if-let [prev (posts/prev-post-id id-num)] 
					[:a.nav {:href (str "/" prev)} "previous"] 
					[:span.nav "this is the earliest post!"])
				(if-let [nxt (posts/next-post-id id-num)] 
					[:a.nav {:href (str "/" nxt)} "next"] 
					[:span.nav "this is the latest post"])])
			[:p (hiccup/h body)]
			[:hr]
			[:a {:href (str id "/comment#comments"), :name "comments"} "comment"]
			(map display-comment (posts/get-comments id)))))

(defn add-comment [post-id]
	(layout "be mean on the net"
		(view-post post-id)
		[:hr]
		(form/form-to [:post (str "/" post-id)]
			(form/text-field "username" "name")
			[:br][:br]
			(form/text-area {:rows 5, :cols 80} "body" "comment")
			[:br]
			[:button {:type "submit" :class "btn btn-defualt"} "save"])))

(defn view-user [user-id]
	(layout "check this guy out"
		(if-let [{:keys [username join_date]} (posts/get-user user-id)]
			[:section [:h1 username] [:p "joined " join_date][:hr]])
		(if-let [{:keys [street_1 street_2 city state zip]} (posts/get-addr user-id)]
			[:p street_1 [:br] street_2 [:br] city [:br] state ", " zip]
			)))


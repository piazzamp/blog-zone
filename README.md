# blog-zone

A super-basic blogging platform based on [xunawu](//www.xuan-wu.com)'s clojure webapp
tutorial. The tutorial left out a few crucial facets of the application (updating a
post, for example) and I tried to add some features like post navigation and commenting. 
I started this project to learn the basics of clojure / compojure 
web app development. It is a blast to work on. 

####Lessons Learned
Getting started with Clojure web applications is about a thousand times faster
than Java, even (especially?) when using a Java framework.

####TO-DOs
 + add ability for admins to delete or moderate comments
 + add footer
 + finish comments (add comment page, see all comments by user, etc)
 + favicon!

## Prerequisites

You will need 
+ [Leiningen][1] 1.7.0
+ [Compojure][2] 1.1.8
+ [java.jdbc][3] 0.3.5
+ mysql-connector-java 5.1.25
+ [ring-basic-authentication][4] 1.0.5 (just until I get around to putting [friend][5] on here!)

[1]: https://github.com/technomancy/leiningen
[2]: https://github.com/weavejester/compojure
[3]: https://github.com/clojure/java.jdbc
[4]: https://github.com/remvee/ring-basic-authentication
[5]: https://github.com/cemerick/friend

## Running

To start a web server for the application, run ``lein ring server-headless``
from the blog-zone directory then navigate to ``localhost:3000`` in your 
favorite browser or whichever port ring tells you that it has
started on (you can change this in project.clj)
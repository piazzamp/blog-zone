/*
Here find the required (or default) ddl for the blog-zone database tables.
They are in a mysql dialect to match my setup with the mysql jdbc connector used in
blog-zone/posts.
As you may be able to tell from the database var, I made these tables in a database
(aka schema) called 'blog' with a specific application database user.
Execute this script as the root user, identified by a blank password by default :(

Matt Piazza, 19 OCT 2014
*/

create user app_user identified by 'put a password here';

create database blog;

use blog;

create table posts (
	id int primary key,
	title varchar(255), 
	body mediumtext, 
	-- author int references users(user_id)
	created_date datetime, 
	updated_date datetime);

create table comments (
	post_id int, 
	id int primary key,
	comment_id int auto_increment,  -- should make guuid?
	body mediumtext, 
	created_date datetime, 
	updated_date datetime,
	user_id int references users(user_id), 
	foreign key (post_id) 
		references posts (id) 
		on delete cascade);

create table users (
	username varchar(255),
	user_id int primary key auto_increment, -- should make this a guuid?
	password char(41),
	join_date datetime);

create table admins (
	user_id int references users(user_id),
	added_by varchar(255),
	added_date datetime);

create table addresses (
	user_id int references users(user_id),
	street_1 varchar(255),
	street_2 varchar(255),
	city varchar(255),
	state char(2),
	zip char(5));

create table comment_likes ( -- many-to-many join table
	comment_id int references comments(comment_id),
	user_id int references users(user_id));

create table post_likes (
	post_id int references posts(post_id),
	user_id int references users(user_id));

grant all on blog.* to app_user;

commit;
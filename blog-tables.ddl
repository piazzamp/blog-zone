/*
Here find the required (or default) ddl for the blog-zone database tables.
They are in a mysql dialect to match my setup with the mysql jdbc connector used in
blog-zone/posts.
As you may be able to tell from the database var, I made these tables in a database
(aka schema) called 'blog' with a specific application database user.
Execute this script as the root user, identified by a blank password by default :(

Matt Piazza, 16 AUG 2014
*/

create user app_user identified by 'put a password here';

create database blog;

use blog;

create table posts (
	id int primary key,
	title varchar(255), 
	body mediumtext, 
	created_date datetime, 
	updated_date datetime);

create table comments (
	post_id int, 
	comment_id int, 
	body mediumtext, 
	created_date datetime, 
	updated_date datetime, 
	foreign key (post_id) 
		references posts (id) 
		on delete cascade);

grant all on posts to app_user;
grant all on comments to app_user;
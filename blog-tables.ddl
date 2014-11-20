/************************************
 * Here find the required (or default) ddl for the blog-zone database tables.
 * They are in a mysql dialect to match my setup with the mysql jdbc connector used in
 * blog-zone/posts.
 * As you may be able to tell from the database var, I made these tables in a database
 * (aka schema) called 'blog2' with a specific application database user.
 * Execute this script as the root user, identified by a blank password by default :(
 * 
 * Matt Piazza, v2.0 19 NOV 2014
 ************************************/

reate user app_user identified by 'put a password here';

create database blog2;

use blog2;

create table posts (
	id int primary key,
	title varchar(255), 
	body mediumtext, 
	author char(16) references users(user_id),
	created_date datetime default CURRENT_TIMESTAMP, 
	updated_date datetime default CURRENT_TIMESTAMP);

create table comments (
	post_id int, 
	id int primary key auto_increment,
	body mediumtext, 
	created_date datetime, 
	updated_date datetime,
	user_id char(16) references users(user_id), 
	foreign key (post_id) 
		references posts (id) 
		on delete cascade);

create table users (
	username varchar(255) not null unique,
	user_id char(16) primary key, -- guuid datatype, ready for trigger
	password char(41) not null default '*2470C0C06DEE42FD1618BB99005ADCA2EC9D1E19',
	join_date datetime);

create table admins (
	user_id char(16) references users(user_id),
	added_by varchar(255),
	added_date datetime);

create table addresses (
	user_id char(16) references users(user_id),
	street_1 varchar(255),
	street_2 varchar(255),
	city varchar(255),
	state char(2),
	zip char(5));

create table comment_likes ( -- many-to-many join table
	comment_id int references comments(comment_id),
	user_id char(16) references users(user_id));

create table post_likes (
	post_id int references posts(post_id),
	user_id char(16) references users(user_id));

create view top_posts as select * from posts where id in (select post_id from post_likes group by post_id having count(*)>1);

create trigger before_insert_user
	before insert on users
	for each row set new.user_id=UUID();

delimiter //
create function max_post_id() 
	returns int deterministic 
	begin 
		declare result int;
		select max(id) into result from posts;
		if result is null 
			then set result = 1;
		end if;
		return result;
	end;//

create procedure save_post(in ititle varchar(255), in ibody mediumtext, in iauthor char(16))
	begin 
		insert into posts(title, body, author, id) values (ititle, ibody, iauthor, 1+max_post_id());
	end;//

grant all on blog2.* to app_user//

commit//

/* optional starter pack *
	delimiter ;
	insert into users(username,user_id, password, join_date) values('admin', '1', password('password'), CURRENT_TIMESTAMP);
	insert into admins(user_id, added_by, added_date) values((select user_id from users where username = 'admin')), 'chuck', CURRENT_TIMESTAMP);
	insert into posts(title, body, author) 
		values('hello world',
		'here''s the first post, the rest is up to you. login to the admin console as admin/password to get posting!',
		(select user_id from users where username = 'admin'));
*/
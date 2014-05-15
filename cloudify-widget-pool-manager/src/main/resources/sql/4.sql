create table decisions (
  id int not null auto_increment,
  decision_type varchar(200),
  pool_id varchar(200),
  approved boolean,
  details mediumtext,
  primary key (id)
);

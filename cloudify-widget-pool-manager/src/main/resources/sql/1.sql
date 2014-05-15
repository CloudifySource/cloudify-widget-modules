create table nodes (
  id int not null auto_increment,
  pool_id varchar(200),
  node_status varchar(200),
  machine_id varchar(200),
  primary key (id)
);

create table errors (
  id int not null auto_increment,
  task_name varchar(200),
  pool_id varchar(200),
  message varchar(2000),
  info varchar(4000),
  primary key (id)
);

create table tasks (
  id int not null auto_increment,
  task_name varchar(200),
  node_id int,
  pool_id varchar(200),
  primary key (id)
);

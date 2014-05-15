create table resource (
  id BIGINT NOT NULL AUTO_INCREMENT primary key,
  account_id BIGINT NOT NULL,
  resource_content longblob,
  resource_name varchar (255),
  resource_content_type varchar(255),
  resource_size BIGINT
);


-- drop table resource;
CREATE TABLE pool_configuration ( id BIGINT NOT NULL AUTO_INCREMENT, account_id BIGINT, pool_setting TEXT, PRIMARY KEY (id) );
CREATE TABLE account ( id BIGINT NOT NULL AUTO_INCREMENT, uuid VARCHAR(255) DEFAULT NULL, PRIMARY KEY (id) );


-- drop table pool_configuration;
-- drop table patchlevel;
-- drop table account;
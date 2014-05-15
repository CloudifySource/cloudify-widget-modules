alter table pool_configuration add column uuid varchar(64) not null;
update pool_configuration set uuid = UUID();

-- alter table pool_configuration drop uuid ;
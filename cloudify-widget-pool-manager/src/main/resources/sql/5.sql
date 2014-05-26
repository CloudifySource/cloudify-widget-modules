
delete from errors;
alter table errors change task_name source varchar(1024);
alter table errors add column timestamp bigint;

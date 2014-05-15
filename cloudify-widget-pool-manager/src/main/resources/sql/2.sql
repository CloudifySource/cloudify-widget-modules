-- tasks is no longer needed as it's saved to memory
drop table tasks;
-- supporting ssh persistence now as ssh details are not kept in jclouds api after machine is created
alter table nodes add column machine_ssh_details varchar(32768);

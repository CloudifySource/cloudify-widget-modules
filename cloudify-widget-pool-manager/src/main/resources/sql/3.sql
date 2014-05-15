-- removing all records as we have changed the ssh details model. next time we will have to upgrade the data.. :(
delete from nodes;
-- we need larger contents for stack traces etc.
alter table errors modify message mediumtext;
alter table errors modify info mediumtext;

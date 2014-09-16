alter table nodes add column ping_status text;
update nodes set ping_status='{"pingStatus":"NOT_PINGED_YET","timestamp":1388534400000}';

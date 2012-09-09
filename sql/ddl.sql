create table short_trap (
  id int auto_increment primary key,
  open_price double not null,
  is_real char(1) not null default '0'
);

create table configuration (
  conf_key varchar(255) not null primary key,
  conf_value varchar(255)
);


create table history (
  id int auto_increment primary key,
  event_type int not null,
  event_dt datetime,
  price double,
  lots int
);

delimiter //
create trigger short_repeat after update on short_trap
for each row
begin
  if OLD.is_real = 2 and NEW.is_real = 0 then
    insert into history (event_type, event_dt, price) 
        values (0, now(), OLD.open_price);
  end if;
end;//
delimiter ;

create table delete_request (
  id int auto_increment primary key,
  price double not null
);

delimiter //
create trigger short_delete after delete on short_trap
for each row
begin
  insert into history (event_type, event_dt, price)
    values (2, now(), OLD.open_price);
  insert into delete_request (price) values (OLD.open_price);
end;//
delimiter ;

delimiter //
create trigger short_insert after insert on short_trap
for each row
begin
  insert into history (event_type, event_dt, price)
    values (1, now(), NEW.open_price);
end;//
delimiter ;

create table position (
  ticket_no int primary key,

  magic_no int,

  pos_type char(1),
  open_price double,
  tp_price double,
  sl_price double,
  swap_point int,
  profit double,

  is_real char(1) default 0,
  symbol varchar(16),
  lots double,
  is_wide_body char(1) default 1
);

---


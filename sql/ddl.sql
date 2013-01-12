create table short_trap (
  id int auto_increment primary key,
  open_price double not null
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
  lots double,
  ticket_no int
);

create table delete_request (
  id int auto_increment primary key,
  price double not null
);


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

create table settlement_history (
  id int auto_increment primary key,
  settle_type char(1) default 0 not null,
  settle_dt datetime not null,

  balance double not null,
  profit  double not null
);


create table reserved_profit (
  id int auto_increment primary key,
  reserve_dt datetime,

  amount int,
  description text
);

delimiter //
create trigger short_delete after delete on short_trap
for each row
begin
  insert into delete_request (price) values (OLD.open_price);
end;//
delimiter ;


alter table position add (pos_cd char(1) default 0);


create table history_request (
  ticket_no int primary key,
  pos_cd char(1)
);


create table position_history (
  ticket_no int primary key,

  open_dt datetime,
  pos_cd char(1),

  lots double,
  symbol varchar(16),

  open_price double,
  sl_price double,
  tp_price double,

  close_dt datetime,
  close_price double,

  swap_point int,
  profit double
);


delimiter //
create trigger position_delete after delete on position
for each row
begin
  if OLD.magic_no <> 0 then
    insert into history (ticket_no, event_type, event_dt, price, lots)
          values (OLD.ticket_no, 0, now(), OLD.open_price, OLD.lots);
  end if;

  insert into history_request (ticket_no, pos_cd) 
    values (OLD.ticket_no, OLD.pos_cd);
end;//
delimiter ;

alter table position_history add (magic_no int);
alter table position_history add (pos_type char(1));
---



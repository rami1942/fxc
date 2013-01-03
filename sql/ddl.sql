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



delimiter //
create trigger position_insert after insert on position
for each row
begin
  if NEW.magic_no = 0 then
    if NEW.pos_type = 0 then
      insert into history (ticket_no, event_type, event_dt, price, lots)
            values (NEW.ticket_no, 3, now(), NEW.open_price, NEW.lots);
    else
      insert into history (ticket_no, event_type, event_dt, price, lots)
            values (NEW.ticket_no, 5, now(), NEW.open_price, NEW.lots);
    end if;
  end if;
end;//
delimiter ;

create table settlement_history (
  id int auto_increment primary key,
  settle_type char(1) default 0 not null,
  settle_dt datetime not null,

  balance double not null,
  profit  double not null
);

delimiter //
create trigger position_delete after delete on position
for each row
begin
  if OLD.magic_no <> 0 then
    insert into history (ticket_no, event_type, event_dt, price, lots)
          values (OLD.ticket_no, 0, now(), OLD.open_price, OLD.lots);
  else
    if OLD.pos_type = 0 then
      insert into history(ticket_no, event_type, event_dt, price, lots)
            values (OLD.ticket_no, 4, now(), OLD.open_price, OLD.lots);
    else
      insert into history (ticket_no, event_type, event_dt, price, lots)
            values (OLD.ticket_no, 6, now(), OLD.open_price, OLD.lots);
    end if;
  end if;
end;//
delimiter ;

create table reserved_profit (
  id int auto_increment primary key,
  reserve_dt datetime,

  amount int,
  description text
);


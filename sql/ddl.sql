create table short_position (
  id int auto_increment primary key,
  open_price double not null,
  is_real char(1) not null default '0'
);

create table configuration (
  conf_key varchar(255) not null primary key,
  conf_value varchar(255)
);

create table long_position (
  id int auto_increment primary key,
  open_price double not null,
  lots int not null
);

create table history (
  id int auto_increment primary key,
  event_type int not null,
  event_dt datetime,
  price double,
  lots int
);

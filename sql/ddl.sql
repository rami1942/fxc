create table short_position (
  id int auto_increment primary key,
  open_price double not null,
  is_real char(1) not null default '0'
);

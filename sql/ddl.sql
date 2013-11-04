--- ショートトラップ

create table short_trap (
  id int auto_increment primary key,
  open_price double not null
);

--- 設定もろもろ
create table configuration (
  conf_key varchar(255) not null primary key,
  conf_value varchar(255)
);

--- 資金の推移履歴
create table history (
  id int auto_increment primary key,
  event_type int not null,
  event_dt datetime,
  price double,
  lots double,
  ticket_no int
);

--- ポジション削除要求
create table delete_request (
  id int auto_increment primary key,
  price double not null
);


--- 現在生きているポジション
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
  is_wide_body char(1) default 1,
  pos_cd char(1) default 0
);


--- 資金の推移履歴 historyと整理できてない
create table settlement_history (
  id int auto_increment primary key,
  settle_type char(1) default 0 not null,
  settle_dt datetime not null,

  balance double not null,
  profit  double not null
);

--- KKW用の確保益だが..位置づけ変えるか消す
create table reserved_profit (
  id int auto_increment primary key,
  reserve_dt datetime,

  amount int,
  description text
);


---
--- short_trapが削除された時に対応するオーダーをDeleteするリクエストを
--- 発行するトリガー
---

delimiter //
create trigger short_delete after delete on short_trap
for each row
begin
  insert into delete_request (price) values (OLD.open_price);
end;//
delimiter ;

--- ポジションが消えた時にMTにposition_hisotryを挿入要求出すためのTBL

create table history_request (
  ticket_no int primary key,
  pos_cd char(1)
);

--- 過去のポジション

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
  profit double,

  magic_no int,
  pos_type char(1)
);

--- トラップのTPのON/OFF
--- そのうち消す

create table toggle_tp_request (
  ticket_no int primary key,
  tp_price double not null
);

--- 表示位置指定用に追加したカラムだが..消す

alter table position add (
  disp_pos int not null default 0
);


---
--- ポジションが消えた時にポジション履歴挿入のリクエストをかけるトリガー
---

drop trigger position_delete;

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


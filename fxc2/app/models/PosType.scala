package models;

// 値の定義はMQLに準拠する。

object PosType extends Enumeration {
  val OP_BUY = Value(0)
  val OP_SELL = Value(1)
  val OP_BUYLIMIT = Value(2)
  val OP_SELLLIMIT = Value(3)
  val OP_BUYSTOP = Value(4)
  val OP_SELLSTOP = Value(5)
}

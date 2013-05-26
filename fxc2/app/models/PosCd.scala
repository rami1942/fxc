package models;

object PosCd extends Enumeration {
  val Undefined = Value(0)
  val BodyLong = Value(1)
  val CILong = Value(2)
  val DiscLong = Value(3)
  val ShortTrap = Value(4)
  val ExitShort = Value(5)
  val FixedShort = Value(6)
  val DiscShort = Value(7)

  def getLabel(v : PosCd.Value) : String = {
    v match {
      case Undefined => "未定義"
      case BodyLong => "本体ロング"
      case CILong => "複利ロング"
      case DiscLong => "裁量ロング"
      case ShortTrap => "ショートトラップ"
      case ExitShort => "出口益S"
      case FixedShort => "固定ショート"
      case DiscShort => "裁量ショート"
    }
  }
}

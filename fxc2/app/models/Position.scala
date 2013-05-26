package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current

import java.util.Date

case class Position (
  ticketNo : Long,
  magicNo : Long,
  posType : PosType.Value,
  openPrice : Double,
  tpPrice : Double,
  slPrice : Double,
  swapPoint : Long,
  profit : Double,
  isReal : Int,
  symbol : String,
  lots : Double,
  isWideBody : Int,
  posCd : PosCd.Value,
  dispPos : Int
)

object Position {
  var full = {
    get[Long]("ticket_no")~get[Long]("magic_no")~get[String]("pos_type")~
    get[Double]("open_price")~get[Double]("tp_price")~get[Double]("sl_price")~
    get[Long]("swap_point")~get[Double]("profit")~get[String]("is_real")~
    get[String]("symbol")~get[Double]("lots")~get[String]("is_wide_body")~
    get[String]("pos_cd")~get[Int]("disp_pos") map {
      case ticketNo~magicNo~posType~openPrice~tpPrice~slPrice~
           swapPoint~profit~isReal~symbol~lots~isWideBody~posCd~dispPos =>
        Position(ticketNo, magicNo, PosType(posType.toInt), 
                 openPrice, tpPrice, slPrice,
                 swapPoint, profit, isReal.toInt, symbol, lots,
                 isWideBody.toInt, PosCd(posCd.toInt), dispPos)
    }
  }

  def all() : List[Position] = DB.withConnection { implicit c =>
    SQL(
      """
        select ticket_no, magic_no, pos_type,
               open_price, tp_price, sl_price,
               swap_point, profit, is_real, symbol, lots, is_wide_body, 
               pos_cd, disp_pos
        from position
        order by open_price desc
      """).as(full*)
  }

}


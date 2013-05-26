package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current

import java.util.Date

case class SettleSummary (
  lastSettleDt : Date,
  margin : Long,
  balance : Long,
  profit : Long,
  swapPoint : Long,
  balanceDiff : Long,
  profitDiff : Long
)

case class SettlementHistory(
  id : Long,
  settleType : Int,
  settleDt : Date,
  balance : Double,
  profit : Double
)

object SettlementHistory {
  var full = {
    get[Long]("id")~get[String]("settle_type")~get[Date]("settle_dt")~get[Double]("balance")~get[Double]("profit") map {
      case id~settleType~settleDt~balance~profit => SettlementHistory(id, settleType.toInt, settleDt, balance, profit)
    }
  }

  def summary() : SettleSummary = DB.withConnection { implicit c =>
    var balance = Configuration.getByKey("balance").toLong
    var profit = SQL("select sum(profit) + sum(swap_point) from position").as(scalar[Double].single).round

    var last = SQL("select id, settle_type, settle_dt, balance, profit from settlement_history where settle_type=0 order by settle_dt desc limit 1").as(full*)
    var balanceDiff = if (last.size > 0) (balance - last.head.balance.round) else 0
    var profitDiff = if (last.size > 0) (profit - last.head.profit.round) else 0

    SettleSummary(
      if (last.size > 0) last.head.settleDt else new Date(),
      Configuration.getByKey("margin").toLong,
      balance,
      profit,
      SQL("select sum(profit) * 0 + sum(swap_point) from position").as(scalar[Double].single).round,  // "sum(profit) * 0" is Anorm workaround
      balanceDiff,
      profitDiff
    )
  }

  def clear = DB.withConnection { implicit c =>
    SQL(
      """
        insert into settlement_history
          (settle_type, settle_dt, balance, profit) values (
            0, now(), {balance}, {profit})
      """).
    on(
      'balance -> Configuration.getByKey("balance").toDouble,
      'profit -> SQL("select sum(profit) + sum(swap_point) from position").
                   as(scalar[Double].single)
    ).executeInsert()
  }

}

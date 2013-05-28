package controllers

import play.api._
import play.api.mvc._

import models.Configuration
import models.SettlementHistory
import models.Position

import java.util.Date

case class ChartInfo(
  now : Date,
  ask : Double,
  bid : Double,

  widthFactor : Int,
  unitHeight : Int,
  priceHigh : Double,
  priceLow: Double
)

object Application extends Controller {
  
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def summary = Action {
    Ok(views.html.summary(
         SettlementHistory.summary(),
         Position.all,
         Configuration.getByKey("ask").toDouble,
         Configuration.getByKey("current_price").toDouble,
         Configuration.getByKey("auth_key").value))
  }

  def resetSummary = Action {
    SettlementHistory.clear
    Redirect(routes.Application.summary)
  }

  def chart = Action {

/*
    // zoom view
    var unitHeight = 120
    var priceHigh = 105.0
    var priceLow = 96.0

    // wide view
    var unitHeight = 40
    var priceHigh = 105.0
    var priceLow = 80.0
*/

    Ok(views.html.chart(
         ChartInfo(
           new Date(),
           Configuration.getByKey("ask").toDouble,
           Configuration.getByKey("current_price").toDouble,
           Configuration.getByKey("chart:widthFactor").toInt,
           Configuration.getByKey("chart:UnitHeight").toInt,
           Configuration.getByKey("chart:priceHigh").toDouble,
           Configuration.getByKey("chart:priceLow").toDouble),
         Position.all))
  }
}

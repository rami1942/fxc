package controllers

import play.api._
import play.api.mvc._

import models.Configuration
import models.SettlementHistory
import models.Position

object Application extends Controller {
  
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def summary = Action {
    Ok(views.html.summary(
         SettlementHistory.summary(),
         Position.all,
         Configuration.getByKey("ask").toDouble,
         Configuration.getByKey("current_price").toDouble))
  }

  def resetSummary = Action {
    SettlementHistory.clear
    Redirect(routes.Application.summary)
  }
}

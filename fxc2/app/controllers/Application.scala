package controllers

import play.api._
import play.api.mvc._

import models.SettlementHistory

object Application extends Controller {
  
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def summary = Action {
    Ok(views.html.summary(SettlementHistory.summary()))
  }

  def resetSummary = Action {
    SettlementHistory.clear
    Redirect(routes.Application.summary)
  }
  
}

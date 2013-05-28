package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current

case class Configuration(
  key : String,
  value : String 
) {
  def toDouble : Double = value.toDouble
  def toLong : Long = value.toDouble.round
  def toCurrency : String = "%,d".format(toLong)
}

object Configuration {
  val config = {
    get[String]("conf_key")~get[String]("conf_value") map {
      case conf_key~conf_value => Configuration(conf_key, conf_value)
    }
  }

  def getByKey(key:String) = DB.withConnection { implicit c =>
    SQL("select conf_key, conf_value from configuration where conf_key={key}").on('key -> key).as(config*).head
  }

}

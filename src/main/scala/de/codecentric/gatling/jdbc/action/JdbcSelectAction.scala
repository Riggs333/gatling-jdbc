package de.codecentric.gatling.jdbc.action

import io.gatling.commons.stats.OK
import io.gatling.commons.util.TimeHelper
import io.gatling.commons.validation.Success
import io.gatling.core.action.{Action, ChainableAction}
import io.gatling.core.session.{Expression, Session}
import io.gatling.core.stats.StatsEngine
import io.gatling.core.stats.message.ResponseTimings
import io.gatling.core.util.NameGen
import scalikejdbc.{DB, SQL}

import scala.util.Try

/**
  * Created by ronny on 11.05.17.
  */
case class JdbcSelectAction(requestName: Expression[String],
                            what: Expression[String],
                            from: Expression[String],
                            where: Option[Expression[String]],
                            statsEngine: StatsEngine,
                            next: Action) extends JdbcAction {

  override def name: String = genName("jdbcSelect")

  override def execute(session: Session): Unit = {
    val start = TimeHelper.nowMillis
    val validatedWhat = what.apply(session)
    val validatedFrom = from.apply(session)
    val validatedWhere = where.map(w => w.apply(session))

    val sqlString = (validatedWhat, validatedFrom, validatedWhere) match {
      case (Success(whatString), Success(fromString), Some(Success(whereString))) => s"SELECT $whatString FROM $fromString WHERE $whereString"
      case (Success(whatString), Success(fromString), None) => s"SELECT $whatString FROM $fromString"
      case _ => throw new IllegalArgumentException
    }

    val tried = Try(DB autoCommit { implicit session =>
      SQL(sqlString).map(rs => rs.toMap()).list.apply()
    })

    log(start, TimeHelper.nowMillis, tried, requestName, session, statsEngine)

    next ! session
  }

}

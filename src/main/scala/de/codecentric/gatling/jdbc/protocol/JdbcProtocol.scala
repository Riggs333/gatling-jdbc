package de.codecentric.gatling.jdbc.protocol

import akka.actor.ActorSystem
import io.gatling.core.{CoreComponents, protocol}
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.protocol.{Protocol, ProtocolKey}
import scalikejdbc._

/**
  * Created by ronny on 10.05.17.
  */
class JdbcProtocol(url: String, username: String, pwd: String, driver: String) extends Protocol {

  Class.forName(driver)

  ConnectionPool.singleton(url, username, pwd)

}

object JdbcProtocol {

  val jdbcProtocolKey = new ProtocolKey {

    override type Protocol = JdbcProtocol
    override type Components = JdbcComponents

    override def protocolClass: Class[protocol.Protocol] = classOf[JdbcProtocol].asInstanceOf[Class[io.gatling.core.protocol.Protocol]]

    override def defaultProtocolValue(configuration: GatlingConfiguration): JdbcProtocol =
      throw new IllegalStateException("Can't provide a default value for JdbcProtocol")

    override def newComponents(system: ActorSystem, coreComponents: CoreComponents): (JdbcProtocol) => JdbcComponents = {
      protocol => JdbcComponents(protocol)
    }

  }

  def apply(url: String, username: String, pwd: String, driver: String): JdbcProtocol = new JdbcProtocol(url, username, pwd, driver)
}
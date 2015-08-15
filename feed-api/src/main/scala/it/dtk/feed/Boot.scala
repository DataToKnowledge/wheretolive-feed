package it.dtk.feed

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.Ficus._

/**
 * Created by fabiofumarola on 10/08/15.
 */
object Boot extends App {
  val config = ConfigFactory.load("api.conf")
  val name = config.getAs[String]("projectId").get
  implicit val system = ActorSystem(name)

  val service = system.actorOf(FeedService.props(),s"$name-service")

}

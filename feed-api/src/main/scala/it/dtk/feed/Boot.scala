package it.dtk.feed

import akka.actor.{ Props, ActorRef, ActorSystem }
import akka.event.Logging
import com.typesafe.config.ConfigFactory
import it.dtk.feed.Model.FeedSource
import it.dtk.feed.manager.FeedsManager
import net.ceedubs.ficus.Ficus._
import akka.io.IO
import spray.can.Http
import akka.pattern._
import scala.concurrent.duration._

import scala.concurrent.Await

/**
 * Created by fabiofumarola on 10/08/15
 */
object Boot extends App {

  val config = ConfigFactory.load()
  val name = config.as[String]("projectId")

  val system = ActorSystem(name, config)
  val logApp = Logging(system.eventStream, this.getClass.getCanonicalName)

  val feedsManagerActor = system.actorSelection(config.as[String]("feed-manager.path"))

  val apiConfig = ConfigFactory.load("api.conf")
  val interface = apiConfig.as[String]("api.host")
  val port = apiConfig.as[Int]("api.port")
  implicit val apiSystem = ActorSystem("feed-api", apiConfig)
  val service = apiSystem.actorOf(FeedService.props(feedsManagerActor))

  IO(Http) ! Http.Bind(service, interface, port)
  logApp.info("started http api service with bind {} on port {}", interface, port)
}

import manager.FeedsManager._
//  feedsManagerActor ! Add(FeedSource("baritoday", "http://www.baritoday.it/rss", 1439646561160L))
//implicit val timeout = akka.util.Timeout(5 seconds)
//val result = (feedsManagerActor ? FeedsList()).mapTo[FeedsList]
//val r = Await.result(result, 10 seconds)
//println(r)

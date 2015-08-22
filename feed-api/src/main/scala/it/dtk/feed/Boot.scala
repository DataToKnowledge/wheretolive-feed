package it.dtk.feed

import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import akka.io.IO
import com.typesafe.config.ConfigFactory
import it.dtk.cluster.Frontend
import net.ceedubs.ficus.Ficus._
import spray.can.Http

/**
 * Created by fabiofumarola on 10/08/15
 */
object Boot extends App {

  val config = ConfigFactory.load("frontend.conf")
  val name = config.as[String]("app.project-id")

  implicit val system = ActorSystem("ApiSystem", config)
  val logApp = Logging(system.eventStream, this.getClass.getCanonicalName)

  val frontendClusterActor = system.actorOf(Props(classOf[Frontend]), name = "frontend")

  //val apiConfig = ConfigFactory.load("api.conf")
  val interface = config.as[String]("app.api.host")
  val port = config.as[Int]("app.api.port")
  //implicit val apiSystem = ActorSystem("feed-api", apiConfig)
  val service = system.actorOf(FeedService.props(frontendClusterActor))

  IO(Http) ! Http.Bind(service, interface, port)
  logApp.info("started http api service with bind {} on port {}", interface, port)
}

//import manager.SmartFeedsManager._
//  feedsManagerActor ! Add(FeedSource("baritoday", "http://www.baritoday.it/rss", 1439646561160L))
//implicit val timeout = akka.util.Timeout(5 seconds)
//val result = (feedsManagerActor ? FeedsList()).mapTo[FeedsList]
//val r = Await.result(result, 10 seconds)
//println(r)

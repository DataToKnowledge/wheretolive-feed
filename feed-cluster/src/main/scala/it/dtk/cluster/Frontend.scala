package it.dtk.cluster

import java.net.URL

import akka.actor.{Props, ActorSystem, Actor}
import akka.contrib.pattern.ClusterClient
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import it.dtk.cluster.FrontendMasterProtocol._
import it.dtk.feed.Model.FeedInfo
import net.ceedubs.ficus.Ficus._
import scala.concurrent.duration._
import akka.pattern._

import scala.util.{Success,Failure}

object FrontendMasterProtocol {

  case class AddFeed(source: FeedInfo)
  case class DeleteFeed(id: String)
  case class Result(msg: String)
  case class ListFeeds(data: Map[String, FeedInfo] = Map.empty)
  case class FeedFailed(f: FeedInfo, ex: Throwable)
}

/**
 * Created by fabiofumarola on 16/08/15.
 */
class Frontend extends Actor {
  import context.dispatcher
  implicit val timeout = Timeout(20 seconds)

  val config = ConfigFactory.load("frontend.conf")

  val initialContacts = config.as[List[String]]("app.initialContacts")
    .map(path => context.system.actorSelection(path)).toSet

  val clusterProxy = context.actorOf(ClusterClient.props(initialContacts))
  println(clusterProxy)


  override def receive: Receive = {
    case add: AddFeed =>
      (clusterProxy ? add).mapTo[Result] pipeTo sender()

    case del: DeleteFeed =>
      (clusterProxy ? del).mapTo[Result] pipeTo sender()

    case list: ListFeeds =>
      (clusterProxy ? list).mapTo[ListFeeds] pipeTo sender()

    case "ping" =>
      (clusterProxy ? "ping").mapTo[String] pipeTo sender()

  }
}

object FrontendTestMain extends App {

  val config = ConfigFactory.load("frontend.conf")
  val system = ActorSystem("FrontendSystem",config)
  val frontend = system.actorOf(Props(classOf[Frontend]), name = "frontend")

  val url = new URL("http://www.baritoday.it/rss")

  implicit val timeoute = akka.util.Timeout(5 seconds)
  implicit val exec = system.dispatcher

  val r = (frontend ? "ping").mapTo[String]

  r.onComplete {
    case Success(res) => println(res)
    case Failure(ex) => ex.printStackTrace()
  }


}

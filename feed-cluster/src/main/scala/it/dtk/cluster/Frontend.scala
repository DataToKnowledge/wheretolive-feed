package it.dtk.cluster

import java.net.URL

import akka.actor.{ Props, ActorSystem, Actor }
import akka.contrib.pattern.ClusterClient
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import it.dtk.cluster.FrontendMasterProtocol._
import it.dtk.feed.Model.FeedInfo
import net.ceedubs.ficus.Ficus._
import scala.concurrent.duration._
import akka.pattern._

import scala.util.{ Success, Failure }

/**
 * Created by fabiofumarola on 16/08/15.
 */
class Frontend extends Actor {
  import context.dispatcher
  implicit val timeout = Timeout(20 seconds)

  val config = context.system.settings.config

  val initialContacts = config.as[List[String]]("app.initialContacts")
    .map(path => context.system.actorSelection(path)).toSet

  val clusterProxy = context.actorOf(ClusterClient.props(initialContacts))

  val masterActorName = config.as[String]("app.master-actor")

  override def receive: Receive = {
    case add: AddFeed =>
      (clusterProxy ? ClusterClient.Send(masterActorName, add, false)).mapTo[Result] pipeTo sender()

    case del: DeleteFeed =>
      (clusterProxy ? ClusterClient.Send(masterActorName, del, false)).mapTo[Result] pipeTo sender()

    case list: ListFeeds =>
      (clusterProxy ? ClusterClient.Send(masterActorName, list, false)).mapTo[ListFeeds] pipeTo sender()

    case "ping" =>
      (clusterProxy ? ClusterClient.Send(masterActorName, "ping", false)).mapTo[String] pipeTo sender()

    case Snapshot =>
      (clusterProxy ? ClusterClient.Send(masterActorName, Snapshot, false)).mapTo[Result] pipeTo sender()

    case EvaluateFeeds =>
      (clusterProxy ? ClusterClient.Send(masterActorName, EvaluateFeeds, false)).mapTo[Result] pipeTo sender()


  }
}

//object FrontendTestMain extends App {
//
//  val config = ConfigFactory.load("frontend.conf")
//  val system = ActorSystem("FrontendSystem", config)
//  val frontend = system.actorOf(Props(classOf[Frontend]), name = "frontend")
//
//  val url = new URL("http://www.baritoday.it/rss")
//
//  implicit val timeout = akka.util.Timeout(5 seconds)
//  implicit val exec = system.dispatcher
//
//  val feed = FeedInfo(
//    url = url.toString,
//    added = System.currentTimeMillis()
//  )
//
//  val r = (frontend ? AddFeed(feed)).mapTo[Result]
//
//  r.onComplete {
//    case Success(res) => println(res)
//    case Failure(ex)  => ex.printStackTrace()
//  }
//
//  system.scheduler.schedule(1 second, 20 seconds){
//    val l = (frontend ? ListFeeds()).mapTo[ListFeeds]
//
//    l.onComplete {
//      case Success(res) => println(res)
//      case Failure(ex)  => ex.printStackTrace()
//    }
//  }
//
//}

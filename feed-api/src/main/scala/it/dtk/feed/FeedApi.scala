package it.dtk.feed

import akka.actor.{Props, ActorRefFactory, Actor, ActorRef}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import it.dtk.feed.Model._
import it.dtk.feed.producer.FeedsManager
import spray.routing.{ RequestContext, HttpService }
import net.ceedubs.ficus.Ficus._
import org.json4s._
import org.json4s.jackson.Serialization
import spray.httpx.Json4sJacksonSupport
import akka.pattern._

import scala.concurrent.Future
import scala.concurrent.duration._

/**
 * Created by fabiofumarola on 10/08/15.
 */
trait FeedApi extends HttpService with Json4sJacksonSupport {
  import it.dtk.feed.producer.FeedsManager._

  val feedsManagerActor: ActorRef

  val routes = pathPrefix("api" / "feed") {
    path("add") {
      post {
        entity(as[FeedSource]) { feed =>
          complete(addFeed(feed))
        }
      }
    } ~ path("list") {
      get {
        complete(listFeeds())
      }
    } ~ path("delete" / Segment) { feedId =>
      post {
        complete(delFeed(feedId))
      }
    }
  }

  def addFeed(feed: FeedSource): Future[Ack]

  def listFeeds(): Future[Map[String, FeedSource]]

  def delFeed(feedName: String): Future[Ack]
}

object FeedService {

  def props() = Props(classOf[FeedService])
}

class FeedService extends Actor with FeedApi {
  import it.dtk.feed.producer.FeedsManager._
  implicit def executionContext = actorRefFactory.dispatcher
  implicit val timeout = Timeout(5 seconds)
  implicit val system = context.system

  val config = ConfigFactory.load("api.conf")

  override def receive: Receive = runRoute(routes)

  override def listFeeds(): Future[Map[String, FeedSource]] =
    (feedsManagerActor ? ListFeeds).mapTo[Map[String, FeedSource]]

  override def addFeed(feed: FeedSource): Future[Ack] =
    (feedsManagerActor ? Manage(feed)).mapTo[Ack]

  override def delFeed(feedName: String): Future[Ack] =
    (feedsManagerActor ? UnManage(feedName)).mapTo[Ack]

  override val feedsManagerActor: ActorRef = context.actorOf(FeedsManager.props)

  override implicit def actorRefFactory: ActorRefFactory = context

  override implicit def json4sJacksonFormats: Formats = Serialization.formats(NoTypeHints)

}
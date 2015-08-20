package it.dtk.feed

import java.net.URL
import java.util.concurrent.ExecutorService

import akka.actor._
import akka.pattern._
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import it.dtk.feed.Model._
import org.json4s._
import org.json4s.jackson.Serialization
import spray.httpx.Json4sJacksonSupport
import spray.routing.HttpService

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._
import scala.util.{ Failure, Success }
import it.dtk.cluster.FrontendMasterProtocol._

/**
 * Created by fabiofumarola on 10/08/15.
 */
trait FeedApi extends HttpService with Json4sJacksonSupport {

  val routes = pathPrefix("api" / "feed") {
    path("add") {
      post {
        entity(as[FeedSource]) { feed =>
          complete(addFeed(feed))
        }
      }
    } ~ path("add" / "list") {
      post {
        entity(as[List[FeedSource]]) { list =>
          complete(addFeeds(list))
        }
      }
    } ~ path("list") {
      get {
        complete(listFeeds())
      }
    } ~ path("delete" / Segment) { id =>
      get {
        complete(delFeed(id))
      }
    }
  }

  def addFeed(feed: FeedSource): Future[Result]

  def addFeeds(feeds: List[FeedSource]): Future[List[Result]]

  def listFeeds(): Future[ListFeeds]

  def delFeed(id: String): Future[Result]

  implicit val executionContext: ExecutionContext
}

object FeedService {
  def props(feedsManagerActor: ActorRef) = Props(new FeedService(feedsManagerActor))
}

class FeedService(val feedsManagerActor: ActorRef) extends Actor with FeedApi {

  //implicit def executionContext = actorRefFactory.dispatcher
  override implicit val executionContext: ExecutionContext = actorRefFactory.dispatcher
  implicit val timeout = Timeout(5 seconds)
  implicit val system = context.system

  val config = ConfigFactory.load()

  override def receive: Receive = runRoute(routes)

  override def listFeeds(): Future[ListFeeds] =
    (feedsManagerActor ? ListFeeds()).mapTo[ListFeeds]

  override def addFeed(source: FeedSource): Future[Result] = {
    try {
      val url = new URL(source.url)
      val feedInfo = FeedInfo(url.getHost, source.url, System.currentTimeMillis())
      (feedsManagerActor ? AddFeed(feedInfo)).mapTo[Result]
    } catch {
      case ex: Throwable => Future.failed[Result](ex)
    }
  }

  override def addFeeds(list: List[FeedSource]): Future[List[Result]] = {
    val result = list.map(addFeed(_))
    Future.sequence(result)
  }

  override def delFeed(id: String): Future[Result] =
    (feedsManagerActor ? DeleteFeed(id)).mapTo[Result]

  override implicit def actorRefFactory: ActorRefFactory = context

  override implicit def json4sJacksonFormats: Formats = Serialization.formats(NoTypeHints)

}
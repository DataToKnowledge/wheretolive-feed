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

/**
 * Created by fabiofumarola on 10/08/15.
 */
trait FeedApi extends HttpService with Json4sJacksonSupport {
  import manager.FeedsManager._

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
    } ~ path("delete" / Segment) { id =>
      get {
        complete(delFeed(id))
      }
    } ~ path("test") {
      get {
        complete(FeedSource("http://www.baritoday.it/rss"))
      }
    }
  }

  def addFeed(feed: FeedSource): Future[String]

  def listFeeds(): Future[FeedsList]

  def delFeed(id: String): Future[String]

  implicit val executionContext: ExecutionContext
}

object FeedService {
  def props(feedsManagerActor: ActorSelection) = Props(new FeedService(feedsManagerActor))
}

class FeedService(val feedsManagerActor: ActorSelection) extends Actor with FeedApi {

  import manager.FeedsManager._
  //implicit def executionContext = actorRefFactory.dispatcher
  override implicit val executionContext: ExecutionContext = actorRefFactory.dispatcher
  implicit val timeout = Timeout(5 seconds)
  implicit val system = context.system

  val config = ConfigFactory.load()

  override def receive: Receive = runRoute(routes)

  override def listFeeds(): Future[FeedsList] =
    (feedsManagerActor ? FeedsList()).mapTo[FeedsList]

  override def addFeed(source: FeedSource): Future[String] = {
    try {
      val url = new URL(source.url)
      val feedInfo = FeedInfo(url.getHost, source.url, System.currentTimeMillis())
      (feedsManagerActor ? Add(feedInfo)).mapTo[String]
    } catch {
      case ex: Throwable => Future.failed[String](ex)
    }
  }

  override def delFeed(id: String): Future[String] =
    (feedsManagerActor ? Delete(id)).mapTo[String]

  override implicit def actorRefFactory: ActorRefFactory = context

  override implicit def json4sJacksonFormats: Formats = Serialization.formats(NoTypeHints)

}
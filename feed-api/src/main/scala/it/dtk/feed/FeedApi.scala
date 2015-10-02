package it.dtk.feed

import akka.actor._
import akka.pattern._
import akka.util.Timeout
import com.gettyimages.spray.swagger.SwaggerHttpService
import com.typesafe.config.ConfigFactory
import com.wordnik.swagger.annotations._
import it.dtk.cluster.FrontendMasterProtocol._
import it.dtk.feed.Model._
import org.json4s._
import org.json4s.jackson.Serialization
import spray.httpx.Json4sJacksonSupport
import spray.routing.{ HttpServiceActor, HttpService }

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.reflect.runtime.universe._

/**
 * Created by fabiofumarola on 10/08/15.
 */
@Api(value = "/feed", description = "Operations about the feeds", position = 0)
trait FeedApi extends HttpService with Json4sJacksonSupport {

  val routes = addRoute ~ addListRoute ~ listFeedRoute ~ deleteFeed ~ snapshotFeeds ~ evaluateFeeds

  def pathPrefix = "feed"

  @ApiOperation(value = "Add a feed", notes = "", nickname = "addFeed", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "body", value = "Feed with url", dataType = "FeedSource", required = true, paramType = "body")
  ))
  def addRoute = post {
    path(pathPrefix / "add")
    entity(as[FeedSource]) { feed =>
      complete(addFeed(feed))
    }
  }

  @ApiOperation(value = "Add multiple feeds", notes = "", nickname = "addFeeds", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "body", value = "Feed with url", dataType = "FeedSource", required = true, paramType = "body", allowMultiple = true)
  ))
  def addListRoute = post {
    path(pathPrefix / "adds")
    entity(as[List[FeedSource]]) { list =>
      complete(addFeeds(list))
    }
  }

  @ApiOperation(value = "List all the feeds", notes = "", nickname = "listFeeds", httpMethod = "GET")
  def listFeedRoute = get {
    path(pathPrefix / "list")
    complete(listFeeds())
  }

  @ApiOperation(value = "Delete a feed", notes = "", nickname = "delFeed", httpMethod = "DELETE")
  def deleteFeed = delete {
    path(pathPrefix / "delete" / Segment) { id =>
      complete(delFeed(id))
    }
  }

  @ApiOperation(value = "Snapshot the feeds", notes = "", nickname = "snapFeeds", httpMethod = "POST")
  def snapshotFeeds = post {
    path(pathPrefix / "snapshot") {
      complete(snapFeeds())
    }
  }

  @ApiOperation(value = "evaluate all the the feeds", notes = "", nickname = "evalFeeds", httpMethod = "POST")
  def evaluateFeeds = post {
    path(pathPrefix / "evalfeeds") {
      complete(evalFeeds())
    }
  }


  def addFeed(feed: FeedSource): Future[Result]

  def addFeeds(feeds: List[FeedSource]): Future[List[Result]]

  def listFeeds(): Future[ListFeeds]

  def delFeed(id: String): Future[Result]

  def snapFeeds(): Future[Result]

  def evalFeeds(): Future[Result]

  implicit val executionContext: ExecutionContext
}

object FeedService {
  def props(feedsManagerActor: ActorRef) = Props(new FeedService(feedsManagerActor))
}

class FeedService(val feedsManagerActor: ActorRef) extends HttpServiceActor with FeedApi {

  //implicit def executionContext = actorRefFactory.dispatcher
  override implicit val executionContext: ExecutionContext = actorRefFactory.dispatcher
  implicit val timeout = Timeout(5 seconds)
  implicit val system = context.system
  val config = ConfigFactory.load()

  override def receive: Receive = runRoute(swaggerService.routes ~ routes ~ get {
    path("") {
      pathEndOrSingleSlash {
        getFromResource("/swagger-ui/index.html")
      }
    } ~ getFromResourceDirectory("/swagger-ui")
  }
  )

  override def listFeeds(): Future[ListFeeds] =
    (feedsManagerActor ? ListFeeds()).mapTo[ListFeeds]

  override def addFeed(source: FeedSource): Future[Result] = {
    try {
      val feedInfo = FeedInfo(source.url, System.currentTimeMillis())
      (feedsManagerActor ? AddFeed(feedInfo)).mapTo[Result]
    }
    catch {
      case ex: Throwable => Future.failed[Result](ex)
    }
  }

  override def addFeeds(list: List[FeedSource]): Future[List[Result]] = {
    val result = list.map(addFeed(_))
    Future.sequence(result)
  }

  override def delFeed(id: String): Future[Result] =
    (feedsManagerActor ? DeleteFeed(id)).mapTo[Result]

  override def snapFeeds(): Future[Result] =
    (feedsManagerActor ? Snapshot).mapTo[Result]

  override def evalFeeds(): Future[Result] =
    (feedsManagerActor ? EvaluateFeeds).mapTo[Result]

  override def actorRefFactory = context

  override implicit def json4sJacksonFormats: Formats = Serialization.formats(NoTypeHints)

  val swaggerService = new SwaggerHttpService {
    def actorRefFactory = context
    def apiTypes = Seq(typeOf[FeedApi])
    def apiVersion = "2.0"
    def baseUrl = "/" //the url of your api, not swagger's json endpoint
    override def docsPath = "api-docs" //where you want the swagger-json endpoint exposed
  }
}
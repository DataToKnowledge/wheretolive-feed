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
import spray.http.MediaTypes
import spray.httpx.Json4sJacksonSupport
import spray.routing.{ HttpServiceActor, HttpService }

import scala.annotation.meta.field
import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.reflect.runtime.universe._

@ApiModel(description = "a Feed Source")
case class FeedSource(
  @(ApiModelProperty @field)(value = "the url of the feed") url: String)

case class DeleteSource(feedUrl: String)

/**
 * Created by fabiofumarola on 10/08/15.
 */
@Api(value = "/feed", description = "Operations about feeds", position = 0)
trait FeedApi extends HttpService with Json4sJacksonSupport {
  import MediaTypes._

  val routes = addRoute ~ addListRoute ~ listFeedRoute ~ deleteFeed ~ snapshotFeeds ~ evaluateFeeds ~ lWorkers ~ site

  val site = path("swagger") { getFromResource("swagger-ui/index.html") } ~
    getFromResourceDirectory("swagger-ui")

  def pathPrefix = "feed"

  @ApiOperation(value = "Add a feed", response = classOf[Result], notes = "", nickname = "addFeed", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "body", value = "Feed with url", dataType = "it.dtk.feed.FeedSource", required = true, paramType = "body")
  ))
  def addRoute = post {
    path(pathPrefix / "add")
    entity(as[FeedSource]) { feed =>
      respondWithMediaType(`application/json`) {
        complete(addFeed(feed))
      }
    }
  }

  @ApiOperation(value = "Add multiple feeds", response = classOf[Result], notes = "", nickname = "addFeeds", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "body", value = "Feed with url", dataType = "it.dtk.feed.FeedSource", required = true, paramType = "body", allowMultiple = true)
  ))
  def addListRoute = post {
    path(pathPrefix / "adds")
    entity(as[List[FeedSource]]) { list =>
      respondWithMediaType(`application/json`) {
        complete(addFeeds(list))
      }
    }
  }

  @ApiOperation(value = "List all the feeds", response = classOf[ListFeeds], notes = "", nickname = "listFeeds", httpMethod = "GET")
  def listFeedRoute = get {
    path(pathPrefix / "list")
    respondWithMediaType(`application/json`) {
      complete(listFeeds())
    }
  }

  @ApiOperation(value = "Delete a feed", notes = "", nickname = "delFeed", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "body", value = "Feed with url", dataType = "it.dtk.feed.DeleteSource", required = true, paramType = "body", allowMultiple = false)
  ))
  def deleteFeed = post {
    path(pathPrefix / "delete")
    entity(as[DeleteSource]) { feed =>
      respondWithMediaType(`application/json`) {
        complete(delFeed(feed))
      }
    }
  }

  @ApiOperation(value = "Snapshot the feeds", notes = "", nickname = "snapFeeds", httpMethod = "POST")
  def snapshotFeeds = post {
    path(pathPrefix / "snapshot") {
      respondWithMediaType(`application/json`) {
        complete(snapFeeds())
      }
    }
  }

  @ApiOperation(value = "evaluate all the the feeds", notes = "", nickname = "evalFeeds", httpMethod = "POST")
  def evaluateFeeds = post {
    path(pathPrefix / "evalfeeds") {
      respondWithMediaType(`application/json`) {
        complete(evalFeeds())
      }
    }
  }

  def lWorkers = get {
    path(pathPrefix / "workers") {
      respondWithMediaType(`application/json`) {
        complete(listWorkers())
      }
    }
  }

  def addFeed(feed: FeedSource): Future[Result]

  def addFeeds(feeds: List[FeedSource]): Future[List[Result]]

  def listFeeds(): Future[ListFeeds]

  def delFeed(feed: DeleteSource): Future[Result]

  def snapFeeds(): Future[Result]

  def evalFeeds(): Future[Result]

  def listWorkers(): Future[WorkersList]

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

  override def receive: Receive = runRoute(swaggerService.routes ~ routes)

  override def listFeeds(): Future[ListFeeds] =
    (feedsManagerActor ? ListFeeds()).mapTo[ListFeeds]

  override def addFeed(source: FeedSource): Future[Result] = {
    import com.github.nscala_time.time.Imports._
    try {
      val feedInfo = FeedInfo(source.url, System.currentTimeMillis(), dateLastFeed = Some(DateTime.yesterday))
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

  override def delFeed(source: DeleteSource): Future[Result] = {
    (feedsManagerActor ? DeleteFeed(source.feedUrl)).mapTo[Result]
  }

  override def snapFeeds(): Future[Result] =
    (feedsManagerActor ? Snapshot).mapTo[Result]

  override def evalFeeds(): Future[Result] =
    (feedsManagerActor ? EvaluateFeeds).mapTo[Result]

  override def listWorkers(): Future[WorkersList] =
    (feedsManagerActor ? ListWorkers).mapTo[WorkersList]

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
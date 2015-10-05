package it.dtk.feed

import java.util.concurrent.Executors

import akka.actor.{ Actor, ActorLogging, Props }
import akka.routing.{ DefaultResizer, RoundRobinPool }
import it.dtk.feed.Model._
import it.dtk.{ GoseArticleExtractor, FeedParser }
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.concurrent.ExecutionContext

/**
 * Created by fabiofumarola on 09/08/15.
 */
object FeedProcessor {

  def props = Props(classOf[FeedProcessor])

  def routerProps(nrWorkers: Int, lowerBound: Int = 2, upperBound: Int = 5) =
    RoundRobinPool(nrWorkers, Some(DefaultResizer(lowerBound, upperBound))).props(props)
}

class FeedProcessor extends Actor
    with ActorLogging {

  import it.dtk.WebUtils
  //  implicit val ec = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(2))
  import context.dispatcher
  implicit val formats = org.json4s.DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all

  override def receive = {

    case json: String =>
      parse(json).extractOpt[Feed] match {
        case Some(feed) =>
          val response = WebUtils.download(feed.uri)


        case None =>
      }
  }

  override def postStop(): Unit = {
    WebUtils.close()
  }

}

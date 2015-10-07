package it.dtk.feed

import akka.actor.{ Actor, ActorLogging, Props }
import akka.event.Logging
import akka.routing.{ DefaultResizer, RoundRobinPool }
import com.sclasen.akka.kafka.StreamFSM
import it.dtk.feed.Model._
import it.dtk.feed.logic.{ FeedUtil, HttpDownloader }
import it.dtk.kafka.FeedProducerKafka
import org.json4s._
import org.json4s.jackson.JsonMethods._
import net.ceedubs.ficus.Ficus._

import scala.util._

/**
 * Created by fabiofumarola on 09/08/15.
 */
object FeedProcessor {

  def props = Props(classOf[FeedProcessor])

  def routerProps(nrWorkers: Int, lowerBound: Int = 2, upperBound: Int = 6) =
    RoundRobinPool(nrWorkers, Some(DefaultResizer(lowerBound, upperBound))).props(props)
}

class FeedProcessor extends Actor {

  val log = Logging(context.system, this)

  val config = context.system.settings.config

  //  implicit val ec = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(2))
  import context.dispatcher
  implicit val formats = org.json4s.DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all

  val ws = new HttpDownloader
  val kafkaProd = new FeedProducerKafka(
    topic = config.as[String]("kafka.producer.topic"),
    clientId = config.as[String]("kafka.producer.clientId"),
    brokersList = config.as[String]("kafka.brokers")
  )

  override def receive = {

    case json: String =>
      val send = sender
      log.debug(s"got message $json")
      parse(json).extractOpt[Feed] match {
        case Some(feed) =>

          log.debug(s"parsed feed $feed")
          ws.download(feed.uri) onComplete {

            case Success(response) =>
              val contentType = response.header("Content-Type").getOrElse("")
              log.debug(s"download page ${feed.uri} with status ${response.statusText}")
              val html = response.body
              val processedFeed = FeedUtil.processFeedEntry(feed, html, contentType)
              kafkaProd.sendSync(processedFeed)
              log.debug(s"send message to kafka for uri ${feed.uri}")
              send ! StreamFSM.Processed

            case Failure(ex) =>
              log.error(ex, s"cannot process feed with url ${feed.uri}")
              send ! StreamFSM.Processed
          }

        case None =>
          log.error(s"cannot process feed message $json")
          send ! StreamFSM.Processed
      }
  }

  override def postStop(): Unit = {
    ws.close()
    kafkaProd.close()
  }

}

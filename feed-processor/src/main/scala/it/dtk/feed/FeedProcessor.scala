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

  def props(kafkaProd: FeedProducerKafka, kafkaPageProd: FeedProducerKafka, ws: HttpDownloader) =
    Props(classOf[FeedProcessor], kafkaProd, kafkaPageProd, ws)

  def routerProps(kafkaProd: FeedProducerKafka, kafkaPageProd: FeedProducerKafka, ws: HttpDownloader,
                  nrWorkers: Int, lowerBound: Int = 2, upperBound: Int = 4) =
    RoundRobinPool(nrWorkers, Some(DefaultResizer(lowerBound, upperBound))).props(props(kafkaProd, kafkaPageProd, ws))
}

class FeedProcessor(kafkaProd: FeedProducerKafka,
                    kafkaPageProd: FeedProducerKafka,
                    ws: HttpDownloader) extends Actor {

  val log = Logging(context.system, this)

  val config = context.system.settings.config

  //  implicit val ec = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(2))
  import context.dispatcher
  implicit val formats = org.json4s.DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all

  override def receive = {

    case json: String =>
      sender ! StreamFSM.Processed

      log.info(s"got message ${json.substring(0, 50)}")
      parse(json).extractOpt[Feed] match {

        case Some(feed) =>
          log.info(s"parsed feed ${feed.uri}")
          ws.download(feed.uri) onComplete {

            case Success(response) =>
              val contentType = response.header("Content-Type").getOrElse("")
              log.info(s"downloaded with status ${response.statusText} page ${feed.uri}")
              val html = response.body
              val (processedFeed, pageData) = FeedUtil.processFeedEntry(feed, html, contentType)
              kafkaProd.sendSync(processedFeed)
              kafkaPageProd.sendSync(pageData)
              log.info(s"saved processed feed with uri ${feed.uri} to kafka")

            case Failure(ex) =>
              log.error(ex, s"cannot process feed with url ${feed.uri}")
          }

        case None =>
          log.error(s"cannot process feed message $json")
      }
  }

  override def postStop(): Unit = {
  }

}

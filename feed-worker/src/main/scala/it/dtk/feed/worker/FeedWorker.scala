package it.dtk.feed.worker

import java.net.URL

import akka.actor.{ Actor, ActorLogging, Props }
import com.rometools.rome.io.{ SyndFeedInput, XmlReader }
import it.dtk.feed.FeedParser
import it.dtk.feed.Model.{ FeedInfo, FeedSource }
import it.dtk.feed.manager.FeedsManager.ExtractedUrls
import it.dtk.kafka.FeedProducerKafka
import net.ceedubs.ficus.Ficus._

import scala.collection.JavaConversions._
import scala.concurrent.duration._

/**
 * Created by fabiofumarola on 15/08/15.
 */
object FeedWorker {

  def props(feed: FeedInfo) = Props(new FeedWorker(feed))

  case class InternalState(feed: FeedInfo, lastUrls: Set[String] = Set.empty)
}

class FeedWorker(feed: FeedInfo) extends Actor with ActorLogging {
  import FeedWorker._
  val start = "Start"

  var state = InternalState(feed)
  val feedScheduler = new FeedScheduler()
  val httpTimeout = 1 minute
  val config = context.system.settings.config

  val producer = new FeedProducerKafka(
    topic = config.as[String]("kafka.topic"),
    clientId = config.as[String]("projectId"),
    brokersList = config.as[String]("kafka.brokers"))

  self ! start

  override def receive: Receive = {
    case start =>
      log.info("start worker for feed {} with url {}", state.feed.id, state.feed.url)
      val feedInput = new SyndFeedInput()

      var countNew = -1

      try {
        val feedData = feedInput.build(new XmlReader(new URL(state.feed.url)))
        val parsedFeeds = feedData.getEntries.map(FeedParser(_))
        //send to kafka
        parsedFeeds.foreach(producer.sendSync(_))

        val urls = parsedFeeds.map(_.uri).toSet
        context.parent ! ExtractedUrls(state.feed.id, urls.size)

        countNew = (urls diff state.lastUrls).size
        state = state.copy(lastUrls = urls)
        log.info("extracted {} new urls from {}", countNew, feed.id)

      } catch {
        case ex: Throwable =>
          log.error(ex, "error parsing feed {}", state.feed.id)
          feedScheduler.gotException()
          throw ex //send the exception to the manager
      }

      import context.dispatcher
      val when = feedScheduler.when(countNew)
      context.system.scheduler.scheduleOnce(when, self, start)
      log.info("scheduled feed extraction for {} in {} seconds", state.feed.id, when toSeconds)
  }
}

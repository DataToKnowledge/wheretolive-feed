package it.dtk.feed.worker

import java.net.URL

import akka.actor.{ Actor, Props }
import akka.event.Logging
import com.rometools.rome.io.{ SyndFeedInput, XmlReader }
import it.dtk.feed.{ FeedSchedulerUtil, FeedParser }
import it.dtk.feed.Model.FeedInfo
import it.dtk.kafka.FeedProducerKafka
import net.ceedubs.ficus.Ficus._

import scala.collection.JavaConversions._

object FeedWorker {

  case class Result(feed: FeedInfo)

  def props(feed: FeedInfo) = Props(new FeedWorker(feed))
}

class FeedWorker(val feed: FeedInfo) extends Actor {
  import FeedWorker._
  val log = Logging(context.system.eventStream, this.getClass.getCanonicalName)
  val start = "Start"
  val config = context.system.settings.config

  val producer = new FeedProducerKafka(
    topic = config.as[String]("kafka.topic"),
    clientId = config.as[String]("projectId"),
    brokersList = config.as[String]("kafka.brokers"))

  self ! start

  override def receive: Receive = {
    case start =>
      log.info("start worker for feed {} with url {}", feed.id, feed.url)

      try {
        val feedInput = new SyndFeedInput()
        val feedData = feedInput.build(new XmlReader(new URL(feed.url)))
        val filteredFeeds = feedData.getEntries.map(FeedParser(_))
          .filter(f => !feed.lastUrls.contains(f.uri))
        //send to kafka
        filteredFeeds.foreach(producer.sendSync(_))

        val filteredUrls = filteredFeeds.map(_.uri).toSet
        val nextScheduler = FeedSchedulerUtil.when(feed.fScheduler, filteredUrls.size)

        context.parent ! Result(feed.copy(
          lastUrls = filteredUrls,
          countUrl = feed.countUrl + filteredUrls.size,
          fScheduler = nextScheduler))

      } catch {
        case ex: Throwable =>
          log.error(ex, "error parsing feed {}", feed.id)
          val nextScheduler = FeedSchedulerUtil.gotException(feed.fScheduler)
          context.parent ! Result(feed.copy(fScheduler = nextScheduler))
      }
      context.stop(self)
  }
}

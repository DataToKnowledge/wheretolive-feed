package it.dtk.feed

import akka.actor.{ ActorLogging, Props }
import akka.persistence.{ PersistentActor, SnapshotOffer }
import com.rometools.rome.io.{ SyndFeedInput, XmlReader }
import it.dtk.feed.Model.FeedSource
import it.dtk.kafka.FeedProducerKafka

import scala.collection.JavaConversions._
import scala.concurrent.duration._

import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.Ficus._

class FeedScheduler(val initialTime: FiniteDuration = 10 minutes,
  val delta: FiniteDuration = 2 minute) {

  private var lastTime = initialTime
  private val minTime = 4 minutes

  def gotException() = {
    lastTime *= 2
  }

  def when(numNewUrls: Int = -1): FiniteDuration = {
    numNewUrls match {
      case -1 => lastTime

      case x: Int if x > 5 =>
        val newTime = lastTime - delta
        lastTime = if (newTime < minTime) minTime else newTime
        lastTime

      case x: Int if x <= 5 =>
        lastTime = lastTime + delta
        lastTime
    }
  }
}

object FeedDownloader {

  def props(feed: FeedSource) = Props(new FeedDownloader(Some(feed)))

  object Start

  case class InternalState(feed: Option[FeedSource], lastUrls: Set[String] = Set()) {
    def update(urls: Set[String]) = copy(lastUrls = urls)
  }
}

/**
 * Created by fabiofumarola on 09/08/15.
 */
class FeedDownloader(feed: Option[FeedSource] = None) extends PersistentActor with ActorLogging {
  import FeedDownloader._

  val config = ConfigFactory.load

  var state = InternalState(feed)
  val feedScheduler = new FeedScheduler()
  val httpTimeout = 1 minute

  val producer = new FeedProducerKafka(
    topic = config.as[String]("kafka.topic"),
    clientId = config.as[String]("projectId"),
    brokersList = config.as[String]("kafka.brokers"))

  override def receiveRecover: Receive = {
    case SnapshotOffer(_, s: InternalState) =>
      log.info("offered state = {}", s)
      state = s
      //restart the actor
      self ! Start
  }

  override def receiveCommand: Receive = {

    case Start =>
      val feedInput = new SyndFeedInput()

      var countNew = -1

      try {
        val feedData = feedInput.build(new XmlReader(state.feed.get.url))
        val parsedFeeds = feedData.getEntries.map(FeedParser(_))
        //send to kafka
        parsedFeeds.foreach(producer.sendSync(_))

        val urls = parsedFeeds.map(_.uri).toSet
        countNew = (urls diff state.lastUrls).size

      } catch {
        case ex: Throwable =>
          log.error(ex, "error parsing feed {}", state.feed.get.uniqueName)
          feedScheduler.gotException()
      }

      import context.dispatcher
      context.system.scheduler.scheduleOnce(feedScheduler.when(countNew), self, Start)

  }

  override def persistenceId: String = feed.get.uniqueName

}

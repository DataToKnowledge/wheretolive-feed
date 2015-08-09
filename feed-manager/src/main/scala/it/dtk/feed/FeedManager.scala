package it.dtk.feed

import java.net.URL
import scala.collection.JavaConversions._
import akka.actor.{ActorLogging, Props}
import akka.persistence.{PersistentActor, SnapshotOffer}
import com.rometools.rome.io.{SyndFeedInput, XmlReader}
import it.dtk.feed.Model.FeedSource

import scala.concurrent.duration._

class FeedScheduler(val initialTime: Duration = 10 minutes,
  val delta: Duration = 2 minute) {

  private var lastTime = initialTime
  private val minTime = 4 minutes

  def gotException() = {
    lastTime *= 2
  }

  def when(numNewUrls: Int = -1): Duration = {
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

object FeedManager {

  def props = Props(classOf[FeedManager])

  object Start

  case class InternalState(feed: Option[FeedSource], lastUrls: Set[URL] = Set()) {
    def update(urls: Set[URL]) = copy(lastUrls = urls)
  }
}

/**
 * Created by fabiofumarola on 09/08/15.
 */
class FeedManager(feed: Option[FeedSource] = None) extends PersistentActor with ActorLogging {
  import FeedManager._

  var state = InternalState(feed)
  val feedScheduler = new FeedScheduler()

  val httpTimeout = 1 minute

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

      try {
        val feedData = feedInput.build(new XmlReader(state.feed.get.url))

      } catch {
        case ex: Throwable =>
          log.error(ex, "error parsing feed {}", state.feed.get.uniqueName)
          feedScheduler.gotException()
      }

  }

  override def persistenceId: String = feed.get.uniqueName


}

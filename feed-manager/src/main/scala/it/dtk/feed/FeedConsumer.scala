package it.dtk.feed

import akka.actor.{ Actor, ActorLogging, Props }
import akka.routing.{ DefaultResizer, RoundRobinPool }
import com.sclasen.akka.kafka.StreamFSM
import it.dtk.feed.Model._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import org.json4s.{ NoTypeHints, _ }

/**
 * Created by fabiofumarola on 09/08/15.
 */
object FeedConsumer {

  def props = Props(classOf[FeedConsumer])

  def routerProps(nrWorkers: Int, lowerBound: Int = 2, upperBound: Int = 10) =
    RoundRobinPool(nrWorkers, Some(DefaultResizer(lowerBound, upperBound))).props(props)
}

class FeedConsumer extends Actor with ActorLogging {
  implicit val formats = Serialization.formats(NoTypeHints)

  override def receive = {

    case json: String =>
      parse(json).extractOpt[Feed] match {
        case Some(feed) =>
        //extract the html content
        //detect the language
        //extract the main content
        case None =>
          log.error("received unparsable message {}", json)
      }

      sender ! StreamFSM.Processed
  }
}

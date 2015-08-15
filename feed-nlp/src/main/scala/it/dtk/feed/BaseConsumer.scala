package it.dtk.feed

import akka.actor.{ActorLogging, Actor, Props}
import akka.routing.{DefaultResizer, RoundRobinPool}
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.{write, read}

/**
 * Created by fabiofumarola on 09/08/15.
 */
object BaseConsumer {

  def props = Props(classOf[BaseConsumer])

  def routerProps(nrWorkers: Int, lowerBound: Int = 2, upperBound: Int = 10) =
    RoundRobinPool(nrWorkers, Some(DefaultResizer(lowerBound, upperBound))).props(props)
}

class BaseConsumer extends Actor with ActorLogging {
  implicit val formats = Serialization.formats(NoTypeHints)

  override def receive = {

    case json: String =>
//      parse(json).extractOpt[Feed] match {
//        case Some(feed) =>
//        //extract the html content
//        //detect the language
//        //extract the main content
//        case None =>
//          log.error("received unparsable message {}", json)
//      }
//
//      sender ! StreamFSM.Processed
  }
}

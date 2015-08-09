package it.dtk.feed

import akka.actor.PoisonPill
import akka.persistence.{ SnapshotOffer, PersistentActor }
import it.dtk.feed.Model._

/**
 * Created by fabiofumarola on 09/08/15.
 */
object FeedsManager {

  case class Manage(feed: FeedSource)
  case class Ack(msg: String)
  case class UnManage(feed: FeedSource)
  object ListFeeds
}

class FeedsManager extends PersistentActor {

  import FeedsManager._

  var state = Map.empty[String, FeedSource]

  import akka.actor.OneForOneStrategy
  import akka.actor.SupervisorStrategy._
  /*
   * Always restart a Feed actor if anything goes wrong
   */
  override val supervisorStrategy =
    OneForOneStrategy() {
      case _ => Restart
    }

  override def receiveRecover: Receive = {

    case Manage(feed) => state += feed.uniqueName -> feed
    case UnManage(feed) => state -= feed.uniqueName
    case SnapshotOffer(_, snapshot: Map[String, FeedSource]) => state = snapshot
  }

  override def receiveCommand: Receive = {

    case manage: Manage =>
      persist(manage) { data =>
        val downloader = context.actorOf(FeedDownloader.props(data.feed), data.feed.uniqueName)
        sender ! Ack(s"start managing feed ${data.feed.uniqueName}")
      }

    case unmanage: UnManage =>
      persist(unmanage) { data =>
        state -= unmanage.feed.uniqueName
        val child = context.child(data.feed.uniqueName)
        child.foreach(_ ! PoisonPill)
      }

    case "snap" => saveSnapshot(state)
    case "print" => println(state)

  }

  override def persistenceId: String = "feeds-manager"
}

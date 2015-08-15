package it.dtk.feed.producer

import akka.actor.{ ActorSelection, ActorSystem, Props, PoisonPill }
import akka.persistence.{ PersistentActor, SnapshotOffer }
import it.dtk.feed.Model._

/**
 * Created by fabiofumarola on 09/08/15.
 */
object FeedsManager {

  def props = Props(classOf[FeedsManager])

  def actorSelection(systemName: String, host: String, port: Int, name: String): String =
    s"akka.tcp://$systemName@$host:$port/user/$name"

  case class Manage(feed: FeedSource)
  case class Ack(msg: String)
  case class UnManage(feedId: String)
  object ListFeeds

  case class InternalState(feeds: Map[String, FeedSource] = Map.empty) {
    def add(feed: FeedSource) = copy(feeds + (feed.uniqueName -> feed))
    def delete(feedId: String) = copy(feeds - feedId)
  }
}

class FeedsManager extends PersistentActor {

  import FeedsManager._

  var state = InternalState()

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

    case Manage(feed) => state = state add feed
    case UnManage(feed) => state = state delete feed
    case SnapshotOffer(_, snapshot: InternalState) => state = snapshot
  }

  override def receiveCommand: Receive = {

    case manage: Manage =>
      persist(manage) { data =>
        val downloader = context.actorOf(FeedDownloader.props(data.feed), data.feed.uniqueName)
        downloader ! FeedDownloader.Start
        sender ! Ack(s"start managing feed ${data.feed.uniqueName}")
      }
      saveSnapshot(state)

    case unmanage: UnManage =>
      persist(unmanage) { data =>
        state = state delete data.feedId
        val child = context.child(data.feedId)
        child.foreach(_ ! PoisonPill)
      }
      saveSnapshot(state)

    case ListFeeds => sender ! state.feeds

    case "snap" => saveSnapshot(state)
    case "print" => println(state)

  }

  override def persistenceId: String = "feeds-manager"
}

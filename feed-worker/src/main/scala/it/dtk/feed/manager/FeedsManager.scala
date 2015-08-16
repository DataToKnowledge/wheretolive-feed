package it.dtk.feed.manager

import akka.actor.{ PoisonPill, Props, Terminated, ActorLogging }
import akka.persistence.{ RecoveryCompleted, SnapshotOffer, PersistentActor }
import it.dtk.feed.Model._
import it.dtk.feed.worker.FeedWorker
import org.joda.time.DateTime
import net.ceedubs.ficus.Ficus._

object FeedsManager {

  def props() = Props(classOf[FeedsManager])

  def actorSelection(system: String, host: String, port: Int, name: String): String = {
    val actorPath = s"akka.tcp://$system@$host:$port/user/$name"
    actorPath
  }

  case class Add(source: FeedInfo)
  case class Delete(id: String)
  case class FeedsList(feeds: Map[String, FeedInfo] = Map.empty)
  case class ExtractedUrls(id: String, count: Int)

  case class InternalState(feeds: Map[String, FeedInfo] = Map.empty)
}

class FeedsManager extends PersistentActor with ActorLogging {
  import FeedsManager._
  import akka.actor.OneForOneStrategy
  import akka.actor.SupervisorStrategy._

  var state = InternalState()
  /*
   * Always restart a Feed actor if anything goes wrong
   */
  override val supervisorStrategy =
    OneForOneStrategy() {
      case _ =>
        Restart
    }

  override def receiveRecover: Receive = {
    case SnapshotOffer(data, snapshot: InternalState) =>
      log.info("recovered FeedsManager state from {}", new DateTime(data.timestamp))
      state = snapshot

    case RecoveryCompleted =>
      log.info("recovered with state {}", state)
      state.feeds.values.foreach(startWorker(_))
  }

  override def receiveCommand: Receive = {
    case Add(source) =>
      log.info("processing add {}", source)
      println(s"processing add ${source}")
      val result = if (!state.feeds.contains(source.id)) {
        state = state.copy(feeds = state.feeds + (source.id -> source))
        startWorker(source)
        saveSnapshot(state)
        s"added ${source.id}"
      } else s"already consuming a feed with id ${source.id}"

      sender ! result

    case Delete(id) =>
      log.info("processing delete {}", id)
      val result = if (state.feeds.contains(id)) {
        state = state.copy(feeds = state.feeds - id)
        val worker = context.child(id)
        worker.foreach { w =>
          context.unwatch(w)
          w ! PoisonPill
        }
        saveSnapshot(state)
        s"removed ${id}"
      } else s"cannot find a feed with id $id"

      sender ! result

    case f: FeedsList =>
      log.debug("got feedlist request")
      sender ! FeedsList(state.feeds)

    case ExtractedUrls(id, count) =>
      val info = state.feeds.get(id)
      info.foreach { feed =>
        val increment = feed.countUrl + count
        state = state.copy(feeds = state.feeds + (id -> feed.copy(countUrl = increment)))
      }

    case Terminated(actor) =>
      log.error("actor {} was terminated for an error", actor.path.name)

    case x: Any =>
      println(x)
  }

  def startWorker(source: FeedInfo): Unit = {
    val worker = context.actorOf(FeedWorker.props(source), source.id)
    context.watch(worker)
    log.info("start monitoring {}", source)
  }

  override def persistenceId: String = {
    val name = context.system.settings.config.as[String]("projectId") + "-manager"
    log.info("start persistent actor with name {}", name)
    name
  }
}

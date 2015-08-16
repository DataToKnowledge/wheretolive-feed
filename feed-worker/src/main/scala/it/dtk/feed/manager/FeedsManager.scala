package it.dtk.feed.manager

import akka.actor.{PoisonPill, Props, Terminated}
import akka.event.Logging
import akka.persistence.{PersistentActor, RecoveryCompleted, SnapshotOffer}
import it.dtk.feed.Model._
import it.dtk.feed.worker.FeedWorker
import net.ceedubs.ficus.Ficus._
import org.joda.time.DateTime

object FeedsManager {

  //def props() = Props(classOf[FeedsManager])

  def actorSelection(system: String, host: String, port: Int, name: String): String = {
    val actorPath = s"akka.tcp://$system@$host:$port/user/$name"
    actorPath
  }

  case class Add(source: FeedInfo)
  case class Delete(id: String)
  case class FeedsList(feeds: Map[String, FeedInfo] = Map.empty)

  case class InternalState(feeds: Map[String, FeedInfo] = Map.empty)
}

class FeedsManager extends PersistentActor {
  import FeedsManager._
  import akka.actor.OneForOneStrategy
  import akka.actor.SupervisorStrategy._
  val log = Logging(context.system.eventStream, this.getClass.getCanonicalName)

  case class Next(id: String)
  implicit val executor = context.dispatcher

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

    case FeedWorker.Result(feed) =>
      //update feed info
      state = state.copy(feeds = state.feeds + (feed.id -> feed))
      saveSnapshot(state)
      log.info("got {} url from {} next analysis in {} minutes", feed.lastUrls.size, feed.id, feed.fScheduler.time toMinutes)
      context.system.scheduler.scheduleOnce(feed.fScheduler.time, self, Next(feed.id))

    case Next(id) =>
      val optFeed = state.feeds.get(id)
      optFeed.foreach(f => startWorker(f))

    case Terminated(actor) =>
      log.debug("actor {} was terminated for an error", actor.path.name)

    case x: Any =>
      println(x)
  }

  def startWorker(source: FeedInfo): Unit = {
    val worker = context.actorOf(FeedWorker.props(source), name = source.id)
    context.watch(worker)
    log.info("start monitoring {}", source)
  }

  override def persistenceId: String = {
    val name = context.system.settings.config.as[String]("projectId") + "-manager"
    log.info("start persistent actor with name {}", name)
    name
  }
}

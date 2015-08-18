package it.dtk.cluster

import akka.actor.{ ActorSystem, ActorRef, Props, Terminated }
import akka.cluster.Cluster
import akka.contrib.pattern.ClusterReceptionistExtension
import akka.event.Logging
import akka.persistence.{ PersistentActor, RecoveryCompleted, SnapshotMetadata, SnapshotOffer }
import com.typesafe.config.ConfigFactory
import it.dtk.cluster.BackendWorkerProtocol._
import it.dtk.cluster.FrontendMasterProtocol._
import it.dtk.feed.Model.FeedInfo
import org.joda.time.DateTime
import scala.concurrent.duration._
import net.ceedubs.ficus.Ficus._

object Master {
  /**
   * the name of the actor should be the same of the akka.cluster.role
   * @return
   */
  def props() = Props(classOf[Master])
}

class Master extends PersistentActor {
  val log = Logging(context.system.eventStream, this.getClass.getCanonicalName)
  val config = context.system.settings.config
  implicit val executor = context.dispatcher


  //register the actor that should be available for the client
  ClusterReceptionistExtension(context.system).registerService(self)

  var backends = List.empty[ActorRef]
  var jobCounter = 0
  var state = Map.empty[String, FeedInfo]

  case class Start(source: FeedInfo)

  override def receiveRecover: Receive = {
    case SnapshotOffer(meta: SnapshotMetadata, snap: Map[String, FeedInfo]) =>
      log.info("recovered FeedsManager state from {}", new DateTime(meta.timestamp))
      state = snap

    case RecoveryCompleted =>
      log.info("recovered with state {}", state)
      state.values.foreach(startWorker)
  }

  override def receiveCommand: Receive = {
    case AddFeed(source: FeedInfo) =>
      log.info("processing add {}", source)
      val message = if (!state.contains(source.id)) {
        state += source.id -> source
        startWorker(source)
        saveSnapshot(state)
        s"started job for feed: ${source.url}"
      }
      else s"already contained feed: ${source.url}"

      sender() ! Result(message)

    case Start(source) => startWorker(source)

    case DeleteFeed(id) =>
      log.info("processing delete {}", id)
      val msg = if (state.contains(id)) {
        state -= id
        saveSnapshot(state)
        s"removed feed ${id}"
      }
      else s"feed ${id} does not exist"

      sender() ! Result(msg)

    case ListFeeds(_) => sender() ! ListFeeds(state)

    case BackendRegistration if !backends.contains(sender()) =>
      context.watch(sender())
      backends = sender() :: backends

    case Terminated(a) =>
      backends = backends.filterNot(_ == a)

    case FeedJobResult(source) =>
      state += source.id -> source

    case "ping" =>
      sender() ! "pong"


  }

  def startWorker(source: FeedInfo): Unit = {
    backends.isEmpty match {
      case true =>
        context.system.scheduler.scheduleOnce(20 seconds, self, Start(source))
      case false =>
        jobCounter += 1
        backends(jobCounter % backends.size) ! FeedJob(source)
    }
  }

  override def persistenceId: String = {
    val name = Cluster(context.system).selfRoles.find(_.startsWith("feed-manager")) match {
      case Some(role) => role + "-master"
      case None       => "master"
    }
    log.info("start persistent actor with name {}", name)
    name
  }
}

object MasterMain extends App {

//  if (args.isEmpty)
//    throw new Error("specify the port number")
//
//  val port = args(0)
//
//  val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port")
//    .withFallback(ConfigFactory.load("master.conf"))

  val config = ConfigFactory.load("master.conf")
  val system = ActorSystem("ClusterSystem", config)

  val actorName = config.as[String]("app.master-role")
  val master = system.actorOf(Master.props(), actorName)
  println(s"started actor ${master.path.address}")
}

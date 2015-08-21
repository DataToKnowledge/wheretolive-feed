package it.dtk.cluster

import java.net.URL

import akka.actor._
import akka.cluster.{ Member, MemberStatus, Cluster }
import akka.cluster.ClusterEvent.{ CurrentClusterState, MemberUp }
import akka.event.Logging
import com.typesafe.config.ConfigFactory
import it.dtk.cluster.BackendWorkerProtocol._
import com.rometools.rome.io.{ SyndFeedInput, XmlReader }
import it.dtk.feed.Model.FeedInfo
import it.dtk.kafka.FeedProducerKafka
import net.ceedubs.ficus.Ficus._
import scala.collection.JavaConversions._
import it.dtk.feed._

object BackendWorkerProtocol {
  case class FeedJob(source: FeedInfo)
  case class FeedJobResult(source: FeedInfo)
  case object BackendRegistration

}

/**
 * Created by fabiofumarola on 16/08/15.
 */
class Worker extends Actor {
  val config = context.system.settings.config
  val masterRole = config.as[String]("app.master-role")
  val log = Logging(context.system.eventStream, this.getClass.getCanonicalName)

  val cluster = Cluster(context.system)

  // subscribe to cluster changes, MemberUp
  // re-subscribe when restart
  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])
  override def postStop(): Unit = {
    producer.close()
    cluster.unsubscribe(self)
  }

  val producer = new FeedProducerKafka(
    topic = config.as[String]("kafka.topic"),
    clientId = config.as[String]("kafka.consumer-group"),
    brokersList = config.as[String]("kafka.brokers"))

  override def receive: Receive = {

    case msg: FeedJob =>
      val executor = context.actorOf(Props(new WorkExecutor(producer)))
      executor forward msg

    case state: CurrentClusterState =>
      state.members.filter(_.status == MemberStatus.Up) foreach register

    case MemberUp(m) => register(m)

  }

  def register(member: Member): Unit = {
    if (member.hasRole(masterRole))
      context.actorSelection(RootActorPath(member.address) / "user" / masterRole) ! BackendRegistration
  }

}

class WorkExecutor(val producer: FeedProducerKafka) extends Actor {
  val log = Logging(context.system.eventStream, this.getClass.getCanonicalName)

  override def receive: Receive = {

    case FeedJob(source) =>
      log.info("start worker for feed {}", source.url)

      val lastUrls = source.last100Urls.toSet

      try {
        val reader = new SyndFeedInput()
        val rawFeeds = reader.build(new XmlReader(new URL(source.url)))
        val filtered = rawFeeds.getEntries.map(FeedParser(_))
          .filterNot(f => lastUrls.contains(f.uri))

        filtered.foreach(f => log.debug(f.toString))

        filtered.foreach(producer.sendSync(_))

        val filteredUrl = filtered.map(_.uri).toSet
        val nextScheduler = FeedSchedulerUtil.when(source.fScheduler, filteredUrl.size)
        log.info("extracted {} urls for feed {}", filteredUrl.size, source.url)

        val nextIterationUrls = (filteredUrl.toList ++ source.last100Urls).take(100)

        sender() ! FeedJobResult(
          source.copy(
            last100Urls = nextIterationUrls,
            countUrl = source.countUrl + filteredUrl.size,
            fScheduler = nextScheduler))
      }
      catch {
        case ex: Throwable =>
          log.error(ex, "error processing feed {}", source.url)
          val nextScheduler = FeedSchedulerUtil.gotException(source.fScheduler)
          sender() ! FeedJobResult(source.copy(fScheduler = nextScheduler))
      }
      self ! PoisonPill
  }

}

object WorkerMain extends App {
  val port = if (args.isEmpty) "0" else args(0)
  val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
    withFallback(ConfigFactory.load("worker.conf"))

  val actorName = config.as[String]("app.master-role")
  val system = ActorSystem("ClusterSystem", config)
  val worker = system.actorOf(Props(classOf[Worker]), actorName)
  println(s"started actor ${worker.path}")
}
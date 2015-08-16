package it.dtk.feed

import akka.actor.{Props, ActorSystem}
import akka.event.Logging
import com.typesafe.config.ConfigFactory
import it.dtk.feed.manager.FeedsManager
import net.ceedubs.ficus.Ficus._

/**
 * Created by fabiofumarola on 10/08/15.
 */
object FeedsBoot {

  def main(args: Array[String]) {

    if (args.isEmpty)
      println("start with parameter Manager | Worker")
    else {
      args.head match {
        case "Manager" => startManagerNode()
        case "Worker" => startWorkerNode()
      }
    }

  }

  def startManagerNode(): Unit = {
    val config = ConfigFactory.load("manager.conf")
    val name = config.as[String]("akka.name")
    val interface = config.as[String]("akka.remote.netty.tcp.hostname")
    val port = config.as[Int]("akka.remote.netty.tcp.port")

    val system = ActorSystem(name, config)
    val logApp = Logging(system.eventStream, this.getClass.getCanonicalName)
    val managerName = config.as[String]("manager")
    val manager = system.actorOf(Props(classOf[FeedsManager]), managerName)

    val path = FeedsManager.actorSelection(name, interface, port, managerName)
    logApp.info("Feed Manager ready at path {}", path)
  }

  def startWorkerNode(): Unit = {
    val config = ConfigFactory.load("worker.conf")
    val name = config.as[String]("akka.name")
    val interface = config.as[String]("akka.remote.netty.tcp.hostname")
    val port = config.as[Int]("akka.remote.netty.tcp.port")

    val system = ActorSystem(name, config)
    val logApp = Logging(system.eventStream, this.getClass.getCanonicalName)
    logApp.info("{} ready to accept connections", name)
  }

}

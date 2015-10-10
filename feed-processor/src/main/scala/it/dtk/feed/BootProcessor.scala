package it.dtk.feed

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import it.dtk.kafka.ConsumerKafka
import it.dtk.util.HostIp
import net.ceedubs.ficus.Ficus._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Created by fabiofumarola on 06/10/15.
 */
object BootProcessor extends App {

  if (args.isEmpty) {
    println(
      """
        |specify the parameter for network, processor where <...> should be replaced
        | to select the network interface -> network
        | processor -> processor <network_name>""".stripMargin)
    sys.exit(1)
  }

  args.foreach(println(_))

  args(0) match {
    case "network" =>
      HostIp.findAll().foreach(kv => println(s"${kv._1} -> ${kv._2}"))

    case "processor" =>
      val ethName = args(1)
      Starter.startFeedProcessor(ethName)

    case s: String =>
      println(s"unrecognized parameter $s")
  }
}

object Starter {

  def startFeedProcessor(ethName: String): Unit = {
    val config = ConfigFactory.load("application.conf")
    val appName = config.as[String]("app.name")
    val zkConnect = config.as[String]("kafka.zk-address")
    val topic = config.as[String]("kafka.consumer.topic")
    val consumerGroup = appName

    val ipAddress = HostIp.load(ethName).get
    println(s"application $appName online with ip $ipAddress")

    implicit val system = ActorSystem(appName, config)

    val feedProcessorRouter = system.actorOf(FeedProcessor.routerProps(2))

    val feedConsumer = new ConsumerKafka(system, zkConnect, topic,
      consumerGroup, feedProcessorRouter)

    feedConsumer.start().foreach(_ => println("started Feed Processor"))

    sys.addShutdownHook {
      Await.ready(feedConsumer.stop(), 5 seconds)
      system.shutdown()
    }
  }
}

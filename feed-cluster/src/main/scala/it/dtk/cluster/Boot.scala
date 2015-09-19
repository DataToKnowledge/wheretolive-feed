package it.dtk.cluster

import akka.actor.{ Props, ActorRef, ActorSystem }
import com.typesafe.config.{ ConfigParseOptions, ConfigResolveOptions, ConfigFactory, Config }
import net.ceedubs.ficus.Ficus._

/**
 * Created by fabiofumarola on 29/08/15.
 */
object Boot extends App {

  if (args.isEmpty) {
    println(
      """
        |specify the parameter for network, master or worker where <...> should be replaced
        | to select the network interface -> network
        | master -> master <network_name>
        | worker -> worker <network_name> masterIp port""".stripMargin)
    sys.exit(1)
  }

  args(0) match {
    case "network" =>
      HostIp.findAll().foreach(kv => println(s"${kv._1} -> ${kv._2}"))

    case "master" =>
      Starter.startSeed(args.tail.toList)
    case "worker" =>
      Starter.startWorker(args.tail.toList)

    case s: String =>
      println(s"unrecognized parameter $s")
  }
}

object Starter {

  def startSeed(args: List[String]): Unit = {

    val config = loadConfig("master.conf", args)
    val clusterName = config.as[String]("clustering.clusterName")
    implicit val system = ActorSystem(clusterName, config)

    val actorName = config.as[String]("app.master-role")
    val master: ActorRef = system.actorOf(Master.props(), actorName)
    println(s"started actor ${master.path.address}")

    sys.addShutdownHook(system.shutdown())
  }

  def startWorker(args: List[String]): Unit = {

    val config = loadConfig("worker.conf", args)
    val clusterName = config.as[String]("clustering.clusterName")
    implicit val system = ActorSystem(clusterName, config)

    val actorName = config.as[String]("app.master-role")
    val worker = system.actorOf(Props(classOf[Worker]), actorName)
    println(s"started actor ${worker.path}")

    sys.addShutdownHook(system.shutdown())
  }

  private def loadConfig(file: String, args: List[String]): Config = {

    val ethName = args(0)

    val ipAddress = HostIp.load(ethName).getOrElse("127.0.0.1")
    println(s"detected ip $ipAddress")

    val config = ConfigFactory.load(
      file,
      ConfigParseOptions.defaults.setAllowMissing(true),
      ConfigResolveOptions.defaults.setAllowUnresolved(true)
    )

    file match {
      case "master.conf" =>
        ConfigFactory.parseString(
          s"""
            clustering.ip = ${ipAddress}
          """.stripMargin).
          withFallback(config).resolve

      case "worker.conf" =>

        val seedIp = args(1)
        val seedPort = args(2).toInt

        ConfigFactory.parseString(
          s"""
            clustering.ip = ${ipAddress}
            clustering.seed-ip = $seedIp
            clustering.seed-port = $seedPort
          """.stripMargin).
          withFallback(config).resolve
    }
  }
}

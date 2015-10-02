package it.dtk.cluster

import scala.collection.JavaConversions._
import java.net.NetworkInterface

/**
 * Created by fabiofumarola on 16/09/15.
 */
object HostIp {

  def findAll(): Map[String, String] = {
    val interfaces = NetworkInterface.getNetworkInterfaces
    interfaces.flatMap { inet =>
      inet.getInetAddresses.
        map(e => inet.getDisplayName -> e.getHostAddress)
    } toMap
  }

  def load(name: String): Option[String] = findAll().get(name)
}
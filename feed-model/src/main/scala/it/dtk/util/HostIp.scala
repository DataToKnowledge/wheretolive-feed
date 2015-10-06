package it.dtk.util

import java.net.NetworkInterface

import scala.collection.JavaConversions._

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
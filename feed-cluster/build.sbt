name := "feed-cluster"

organization := "it.datatoknowledge"

version := "0.4.3"

scalaVersion := "2.11.7"

val akkaVersion = "2.3.12"

resolvers += "krasserm at bintray" at "http://dl.bintray.com/krasserm/maven"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-remote" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-contrib" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence-experimental" % akkaVersion,
  "org.iq80.leveldb" % "leveldb" % "0.7",
  "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8",
  "net.ceedubs" %% "ficus" % "1.1.2",
  "org.fusesource" % "sigar" % "1.6.4",
  "com.github.krasserm" %% "akka-persistence-kafka" % "0.4"
)

libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
  "org.slf4j" % "slf4j-api" % "1.7.12"
)

Revolver.settings

defaultScalariformSettings
fork in Test := true
fork in run := true

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)

version in Docker := version.value
maintainer in Docker := "info@datatotknowledge.it"
dockerBaseImage := "java:8-jre"
dockerExposedPorts := Seq(5000)
dockerExposedVolumes := Seq("/opt/docker/logs", "/opt/docker/target")
dockerRepository := Option("data2knowledge")
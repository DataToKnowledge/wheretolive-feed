name := "feed-cluster"

organization := "it.datatoknowledge"

version := "0.2.1"

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
  "org.json4s" %% "json4s-jackson" % "3.2.11",
  "com.rometools" % "rome" % "1.5.1",
  "com.typesafe.play" % "play-ws_2.11" % "2.4.2",
  "com.rubiconproject.oss" % "jchronic" % "0.2.6",
  "com.github.nscala-time" %% "nscala-time" % "2.0.0",
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

packageName in Docker := "data2knowledge/" +  packageName.value
version in Docker := version.toString
maintainer in Docker := "info@datatotknowledge.it"
dockerBaseImage := "java:8-jre"
dockerExposedPorts := Seq(5000)
dockerExposedVolumes := Seq("/opt/docker/logs", "/opt/docker/target")

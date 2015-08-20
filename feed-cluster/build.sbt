name := "feed-cluster"

organization := "it.datatoknowledge"

version := "0.0.1"

scalaVersion := "2.11.7"

val akkaVersion = "2.3.12"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-remote" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-contrib" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence-experimental" % akkaVersion,
  "org.iq80.leveldb" % "leveldb" % "0.7",
  "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8",
  "net.ceedubs" %% "ficus" % "1.1.2",
  "org.json4s" %% "json4s-jackson" % "3.2.11",
  "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2",
  "com.typesafe.scala-logging" %% "scala-logging-api" % "2.1.2",
  "org.slf4j" % "slf4j-log4j12" % "1.7.12",
  "com.rometools" % "rome" % "1.5.1",
  "com.typesafe.play" % "play-ws_2.11" % "2.4.2",
  "com.rubiconproject.oss" % "jchronic" % "0.2.6",
  "com.github.nscala-time" %% "nscala-time" % "2.0.0",
  "org.fusesource" % "sigar" % "1.6.4"
)

Revolver.settings

defaultScalariformSettings
fork in Test := true
fork in run := true

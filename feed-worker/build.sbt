name := "feed-worker"

organization := "it.datatoknowledge"

version := "0.0.1"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.12",
  "com.typesafe.akka" %% "akka-remote" % "2.3.12",
  "com.typesafe.akka" %% "akka-slf4j" % "2.3.12",
  "com.typesafe.akka" %% "akka-persistence-experimental" % "2.3.12",
  "org.iq80.leveldb" % "leveldb" % "0.7",
  "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8",
  "org.apache.kafka" %% "kafka" % "0.8.2.1",
  "net.ceedubs" %% "ficus" % "1.1.2",
  "org.json4s" %% "json4s-jackson" % "3.2.11",
  "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2",
  "com.typesafe.scala-logging" %% "scala-logging-api" % "2.1.2",
  "org.slf4j" % "slf4j-log4j12" % "1.7.12",
  "com.rometools" % "rome" % "1.5.1",
  "com.typesafe.play" % "play-ws_2.11" % "2.4.2",
  "com.rubiconproject.oss" % "jchronic" % "0.2.6",
  "com.github.nscala-time" %% "nscala-time" % "2.0.0"
)

Revolver.settings

defaultScalariformSettings
fork in Test := true
fork := true

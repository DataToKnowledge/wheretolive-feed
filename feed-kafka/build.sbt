name := "feed-kafka"

organization := "it.datatoknowledge"

version := "0.0.1"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.12",
  "com.typesafe.akka" %% "akka-persistence-experimental" % "2.3.12",
  "org.apache.kafka" %% "kafka" % "0.8.2.1",
  "net.ceedubs" %% "ficus" % "1.1.2",
  "com.sclasen" %% "akka-kafka" % "0.1.0",
  "org.json4s" %% "json4s-jackson" % "3.2.11",
  "com.rometools" % "rome" % "1.5.1",
  "org.apache.kafka" % "kafka-clients" % "0.8.2.1"
)

libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
  "org.slf4j" % "slf4j-api" % "1.7.12"
)

Revolver.settings

defaultScalariformSettings

name := "feed-processor"

organization := "it.datatoknowledge"

version := "0.0.1"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.12",
  "com.typesafe.akka" %% "akka-persistence-experimental" % "2.3.12",
  "net.ceedubs" %% "ficus" % "1.1.2",
  "com.github.nscala-time" %% "nscala-time" % "2.0.0"
)

libraryDependencies ++= Seq(
  "com.syncthemall" % "boilerpipe" % "1.2.2",
  "org.apache.tika" % "tika-core" % "1.10",
  "org.apache.tika" % "tika-parsers" % "1.10",
  "com.typesafe.play" %% "play-ws" % "2.4.3",
  "org.json4s" %% "json4s-jackson" % "3.3.0",
  "org.json4s" %% "json4s-ext" % "3.3.0",
  "com.intenthq" %% "gander" % "1.2"
)

libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
  "org.slf4j" % "slf4j-api" % "1.7.12"
)

Revolver.settings

defaultScalariformSettings

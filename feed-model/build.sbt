name := "feed-model"

organization := "it.datatoknowledge"

version := "0.3.1"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.github.nscala-time" %% "nscala-time" % "2.0.0",
  "com.syncthemall" % "boilerpipe" % "1.2.2",
  "org.apache.tika" % "tika-core" % "1.10",
  "org.apache.tika" % "tika-parsers" % "1.10",
  "com.typesafe.play" %% "play-ws" % "2.4.3",
  "org.json4s" %% "json4s-jackson" % "3.3.0",
  "org.json4s" %% "json4s-ext" % "3.3.0",
  "com.intenthq" %% "gander" % "1.2",
  "com.rometools" % "rome" % "1.5.1",
  "org.jsoup" % "jsoup" % "1.8.3"
)

libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
    "org.slf4j" % "slf4j-api" % "1.7.12"
)

Revolver.settings

defaultScalariformSettings

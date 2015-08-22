name := "feed-model"

organization := "it.datatoknowledge"

version := "0.0.1"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.rubiconproject.oss" % "jchronic" % "0.2.6",
  "com.github.nscala-time" %% "nscala-time" % "2.0.0"
)

libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
    "org.slf4j" % "slf4j-api" % "1.7.12"
)

Revolver.settings

defaultScalariformSettings

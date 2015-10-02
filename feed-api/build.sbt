name := "feed-api"

version := "0.2.1"

scalaVersion := "2.11.7"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers ++= Seq(
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
  "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases",
  "Maven central" at "http://repo1.maven.org/maven2/",
  "spray repo" at "http://repo.spray.io"
)

val akkaVersion = "2.3.12"
val sprayV = "1.3.3"

libraryDependencies ++= Seq(
  "io.spray" %% "spray-can" % sprayV,
  "io.spray" %% "spray-routing" % sprayV,
  "org.json4s" %% "json4s-jackson" % "3.2.11",
  "com.gettyimages" %% "spray-swagger" % "0.5.1",
  "net.ceedubs" %% "ficus" % "1.1.2",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-remote" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-contrib" % akkaVersion,
  "io.spray" %% "spray-testkit" % sprayV % "test",
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
  "org.scalatest" %% "scalatest" % "2.2.5" % "test",
  "org.scalactic" %% "scalactic" % "2.2.5",
  "org.scalacheck" %% "scalacheck" % "1.12.3" % "test"
)

libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
   "org.slf4j" % "slf4j-api" % "1.7.12"
)

Revolver.settings
fork in Test := true
fork := true

defaultScalariformSettings

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)

packageName in Docker := "data2knowledge/" +  packageName.value
maintainer in Docker := "info@datatotknowledge.it"
version in Docker := version.toString
dockerBaseImage := "java:8-jre"
dockerExposedPorts := Seq(9000,5000)
dockerExposedVolumes := Seq("/opt/docker/logs")
dockerRepository := Option("data2knowledge")
name := "feed-api"

version := "0.1"

scalaVersion := "2.11.7"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers ++= Seq(
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
  "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases",
  "Maven central" at "http://repo1.maven.org/maven2/",
  "spray repo" at "http://repo.spray.io"
)

libraryDependencies ++= {

  val akkaV = "2.3.12"
  val sprayV = "1.3.3"

  val spray = Seq(
    "io.spray" %% "spray-can" % sprayV,
    "io.spray" %% "spray-routing" % sprayV,
    "org.json4s" %% "json4s-jackson" % "3.2.11"
  )

  val commons = Seq(
    "net.ceedubs" %% "ficus" % "1.1.2"
  )

  val akka = Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-slf4j" % akkaV,
    "com.typesafe.akka" %% "akka-contrib" % akkaV,
    "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2",
    "com.typesafe.scala-logging" %% "scala-logging-api" % "2.1.2",
    "ch.qos.logback" % "logback-classic" % "1.1.3"
  )

  val test = Seq(
    "io.spray" %% "spray-testkit" % sprayV % "test",
    "com.typesafe.akka" %% "akka-testkit" % akkaV % "test",
    "org.scalatest" %% "scalatest" % "2.2.5" % "test",
    "org.scalactic" %% "scalactic" % "2.2.5",
    "org.scalacheck" %% "scalacheck" % "1.12.3" % "test"
  )

  spray ++ akka ++ test ++ commons
}

javaOptions += "-Xms512m -Xmx2G"
Revolver.settings


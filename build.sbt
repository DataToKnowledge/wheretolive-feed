lazy val commons = Seq(
  organization := "it.datatoknowledge",
  version := "0.2.1",
  scalaVersion := "2.11.7",
  scalacOptions ++= Seq("-target:jvm-1.7", "-feature"),
  resolvers ++= Seq(
    "spray repo" at "http://repo.spray.io"
  )
)

lazy val root = (project in file("."))
  .settings(commons: _*)
  .settings(
    name := "wheretolive-feeder",
    run := {
      (run in Compile).evaluated
    }
  ) aggregate(api, kafka, model, cluster)

lazy val api = (project in file("./feed-api"))
  .settings(commons: _*)
  .settings(name := "feed-api")
  .dependsOn(kafka, model, cluster)

lazy val cluster = (project in file("./feed-cluster"))
  .settings(commons: _*)
  .settings(name := "feed-cluster")
  .dependsOn(kafka, model)

lazy val kafka = (project in file("./feed-kafka"))
  .settings(commons: _*)
  .settings(name := "feed-kafka")
  .dependsOn(model)

lazy val model = (project in file("./feed-model"))
  .settings(commons: _*)
  .settings(name := "feed-model")

lazy val processor = (project in file("./feed-processor"))
  .settings(commons: _*)
  .settings(name := "feed-processor")
  .dependsOn(model, kafka)

//libraryDependencies ~= { _.map(_.exclude("org.slf4j", "slf4j-api:1.7.7")) }
//libraryDependencies ~= { _.map(_.exclude("org.slf4j", "slf4j-log4j12:1.6.1")) }

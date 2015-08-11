lazy val commons = Seq(
  organization := "it.datatoknowledge",
  version := "0.0.1",
  scalaVersion := "2.11.7",
  scalacOptions += "-target:jvm-1.7",
  resolvers ++= Seq(
    "spray repo" at "http://repo.spray.io"
  )
)

lazy val root = (project in file("."))
  .settings(commons :_*)
  .settings(
    name := "wheretolive-feeder",
    run := {
      (run in Compile).evaluated
    }
  )aggregate(api,kafka,model,manager)

lazy val api = (project in file("./feed-api"))
  .settings(commons :_*)
  .settings(
    name := "feed-api"
  ).dependsOn(kafka,model,manager)

lazy val manager = (project in file("./feed-manager"))
  .settings(commons :_*)
  .settings(
    name := "feed-manager"
  ).dependsOn(kafka,model)

lazy val kafka = (project in file("./feed-kafka"))
  .settings(commons: _*)
  .settings(name := "feed-kafka")
  .dependsOn(model)

lazy val model = (project in file("./feed-model"))
  .settings(commons :_*)
  .settings(
    name := "feed-model"
  )

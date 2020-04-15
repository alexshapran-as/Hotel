name := "Hotel"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies ++=  Seq(
  "com.softwaremill.akka-http-session" %% "core" % "0.5.10",
  "com.typesafe.akka" %% "akka-actor" % "2.5.25",
  "com.typesafe.akka" %% "akka-http"   % "10.1.8",
  "com.typesafe.akka" %% "akka-stream" % "2.5.25",
  "org.json4s" %% "json4s-native" % "3.6.5",
  "org.mongodb" %% "casbah" % "3.1.1"
)

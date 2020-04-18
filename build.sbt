name := "Hotel"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies ++=  Seq(
  "com.softwaremill.akka-http-session" %% "core" % "0.5.10",
  "com.typesafe.akka" %% "akka-actor" % "2.5.25",
  "com.typesafe.akka" %% "akka-slf4j" % "2.5.25",
  "com.typesafe.akka" %% "akka-stream" % "2.5.25",
  "com.typesafe.akka" %% "akka-http"   % "10.1.8",
  "org.json4s" %% "json4s-native" % "3.6.5",
  "org.mongodb" %% "casbah" % "3.1.1",
  "org.apache.logging.log4j" % "log4j-api" % "2.11.2",
  "org.apache.logging.log4j" % "log4j-core" % "2.11.2",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "net.logstash.logback" % "logstash-logback-encoder" % "5.3"
)

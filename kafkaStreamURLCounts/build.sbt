name := "kafka-Streaming"

version := "0.1"

scalaVersion := "2.11.11"

libraryDependencies ++= Seq(
  // akka
  "com.typesafe.akka" %% "akka-actor" % "2.4.2",
  "com.typesafe.akka" %% "akka-testkit" % "2.4.2" % "test",
  // streams
  "com.typesafe.akka" %% "akka-stream" % "2.4.2",
  // akka http
  "com.typesafe.akka" %% "akka-http-core" % "2.4.2",
  "com.typesafe.akka" %% "akka-http-experimental" % "2.4.2",
  "com.typesafe.akka" %% "akka-http-testkit" % "2.4.2" % "test",
  // the next one add only if you need Spray JSON support
  //"com.typesafe.akka" %% "akka-http-spray-json-experimental" % "2.4.2",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test")

val sparkVersion = "1.6.1"



libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "2.2.0",
  "org.apache.spark" %% "spark-streaming" % "2.2.0"
  //"org.apache.spark" %% "spark-sql" % "2.4.4"
)

libraryDependencies += "org.apache.kafka" %% "kafka" % "1.0.0"
libraryDependencies += "org.apache.kafka" % "kafka-streams" % "1.0.0"
libraryDependencies += "org.apache.spark" % "spark-streaming-kafka-0-10_2.11" % "2.1.0"
libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.10.0"
libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.24"



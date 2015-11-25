

lazy val commonSettings = Seq(
  organization := "jkugiya",
  scalaVersion := "2.11.7",
  libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-stream-experimental_2.11" % "2.0-M1",
  "com.typesafe.akka" % "akka-http-core-experimental_2.11" % "2.0-M1",
  "com.typesafe.akka" % "akka-http-experimental_2.11" % "2.0-M1",
  "org.mockito" % "mockito-core" % "1.10.19" % "test",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"))

lazy val twirl_sample = project
  .settings(commonSettings: _*)
  .enablePlugins(SbtTwirl)

lazy val di_examples = project
  .settings(commonSettings: _*)




lazy val commonSettings = Seq(
  organization := "jkugiya",
  scalaVersion := "2.11.7",
  resolvers ++= Seq(
    "anormcypher" at "http://repo.anormcypher.org/",
    "bintray-sbt-plugin-releases" at "http://dl.bintray.com/content/sbt/sbt-plugin-releases",
    "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
  ),
  libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-stream-experimental_2.11" % "2.0.3",
    "com.typesafe.akka" % "akka-http-core-experimental_2.11" % "2.0.3",
  "com.typesafe.akka" % "akka-http-experimental_2.11" % "2.0.3",
  "com.typesafe.akka" % "akka-http-testkit-experimental_2.11" % "2.0.3",
  "com.google.inject" % "guice" % "4.0",
  "org.anormcypher" %% "anormcypher" % "0.8.x",
  "io.spray" %%  "spray-json" % "1.3.2",
  "org.mockito" % "mockito-core" % "1.10.19" % "test",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"))

lazy val twirl_sample = project
  .settings(commonSettings: _*)
  .enablePlugins(SbtTwirl)

lazy val neo_order_management = project
  .settings(commonSettings: _*)
  .enablePlugins(SbtTwirl)

lazy val di_examples = project
  .settings(commonSettings: _*)

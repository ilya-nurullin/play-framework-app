name := """whipcakeApi"""
organization := "com.whipcake.api"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.3"

libraryDependencies ++= Seq(
  guice, filters, ws, jdbc, jodaForms,
  //"com.typesafe.play" %% "play-json" % "2.6.0",
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.0" % Test,
  "com.typesafe.play" %% "play-slick" % "3.0.0",
  "mysql" % "mysql-connector-java" % "6.0.6",
  "com.github.tototoshi" %% "slick-joda-mapper" % "2.3.0",
  "joda-time" % "joda-time" % "2.9.9",
  "com.typesafe.play" %% "play-json-joda" % "2.6.2",
  "org.joda" % "joda-convert" % "1.8.2",
  "org.mindrot" % "jbcrypt" % "0.4",
  "com.typesafe.play" %% "play-mailer" % "6.0.0",
  "com.typesafe.play" %% "play-mailer-guice" % "6.0.0"
)

javaOptions in Test += "-Dlogger.file=test/resources/logback-test.xml"

javaOptions += "-Duser.timezone=UTC"

sources in (Compile, doc) := Seq.empty

publishArtifact in (Compile, packageDoc) := false

testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oCK")

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.whipcake.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.whipcake.binders._"

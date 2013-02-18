name := "AkkaConcurrency"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.10.0"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++=
    "com.typesafe.akka" %% "akka-actor" % "2.1.0" ::
    "com.typesafe.akka" %% "akka-testkit" % "2.1.0" % "test" ::
    "org.scalatest" %% "scalatest" % "2.0.M5b" % "test" ::
    Nil


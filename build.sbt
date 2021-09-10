name := """play-pac4j-with-sign-in-with-Google"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.6"

libraryDependencies += guice
// libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test

// for play-pac4j
val playPac4jVersion = "11.0.0-PLAY2.8"
val pac4jVersion = "5.1.3"
// libraryDependencies += ehcache
libraryDependencies += caffeine
libraryDependencies += "org.pac4j" %% "play-pac4j" % playPac4jVersion
libraryDependencies += "org.pac4j" % "pac4j-oauth" % pac4jVersion
libraryDependencies += "org.pac4j" % "pac4j-oidc" % pac4jVersion

dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-databind" % "2.11.4"

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"

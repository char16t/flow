ThisBuild / organization := "com.manenkov"
ThisBuild / version := "0.1.0-RC3"
ThisBuild / scalaVersion := "2.13.8"
ThisBuild / crossScalaVersions := Seq(/*"2.10.7", "2.11.12", "2.12.15", */"2.13.8", "3.1.2")

lazy val root = (project in file("."))
  .settings(
    name := "flow"
  )

libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.12"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.12" % Test

resolvers += "Artima Maven Repository" at "https://repo.artima.com/releases"

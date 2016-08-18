name := "di"

organization := "io.souce"

version := "1.0"

scalaVersion := "2.11.8"

enablePlugins(spray.boilerplate.BoilerplatePlugin)

sourceManaged in Compile := baseDirectory.value / "src_managed"

managedSourceDirectories in Compile += baseDirectory.value / "src_managed"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0" % "test"

scalacOptions in ThisBuild ++= Seq("-feature")
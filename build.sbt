name := "disciple"

organization := "io.ics"

version := "1.0"

scalaVersion := "2.11.8"

enablePlugins(spray.boilerplate.BoilerplatePlugin)

sourceManaged in Compile := baseDirectory.value / "src_managed"

managedSourceDirectories in Compile += baseDirectory.value / "src_managed"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0" % "test"

scalacOptions in ThisBuild ++= Seq("-feature")

bintrayVcsUrl := Some("git@github.com:KORPSE/disciple.git")

licenses += ("Apache-2.0", url("https://opensource.org/licenses/apache-2.0"))

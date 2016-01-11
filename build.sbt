import play.sbt.PlayImport._
import play.sbt.routes.RoutesKeys._


name         := "medline-geo-backend"
organization := "ch.alexmass"
version      := "0.1"
scalaVersion := Version.scala

//offline := true

//skip in update := true

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaSource in Compile <<= baseDirectory / "src/scala"

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

libraryDependencies ++= Dependencies.sparkAkkaHadoop

libraryDependencies ++= Seq(
  cache,
  specs2 % Test,
  "com.esotericsoftware.kryo" % "kryo" % "2.24.0",
  "com.google.maps" % "google-maps-services" % "0.1.7"
)

dependencyOverrides ++= Set(
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.4.4"
)

javaOptions in Test += "-Dconfig.file=conf/application.test.conf"

mappings in Universal := {
  val origMappings = (mappings in Universal).value
  origMappings.filterNot { case (_, file) => file.endsWith("private.conf") || file.endsWith("test.conf") }
}

mappings in Universal ++=
  (baseDirectory.value / "data/disk-cache" ** "*" get) map
    (x => x -> ("data/disk-cache/" + x.getPath))

releaseSettings

scalariformSettings

ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }

routesGenerator := InjectedRoutesGenerator

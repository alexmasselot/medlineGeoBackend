import play.sbt.PlayImport._
import play.sbt.routes.RoutesKeys._


name         := "spark-play-activator"
organization := "ch.alexmass"
version      := "0.0.1"
scalaVersion := Version.scala



lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaSource in Compile <<= baseDirectory / "src/scala"

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

libraryDependencies ++= Dependencies.sparkAkkaHadoop

libraryDependencies ++= Seq(
  specs2 % Test,
  "com.esotericsoftware.kryo" % "kryo" % "2.24.0"
)

dependencyOverrides ++= Set(
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.4.4"
)

javaOptions in Test += "-Dconfig.file=conf/application.test.conf"

releaseSettings

scalariformSettings

ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }

//routesGenerator := InjectedRoutesGenerator


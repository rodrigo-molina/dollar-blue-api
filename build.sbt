scalaVersion := "2.13.1"


name := "dolar-blue-api"
organization := "rodrigomolina.dolarblue"
version := "1.0"

val http4sVersion = "0.21.3"

libraryDependencies += "org.typelevel" %% "cats-core" % "2.0.0"
libraryDependencies += "org.typelevel" %% "cats-effect" % "2.1.3"
libraryDependencies += "io.spray" %%  "spray-json" % "1.3.5"
libraryDependencies += "org.http4s" %% "http4s-dsl" % http4sVersion
libraryDependencies += "org.http4s" %% "http4s-blaze-server" % http4sVersion

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-circe" % http4sVersion,
  // Optional for auto-derivation of JSON codecs
  "io.circe" %% "circe-generic" % "0.13.0",
  // Optional for string interpolation to JSON model
  "io.circe" %% "circe-literal" % "0.13.0"
)

//addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

libraryDependencies += "org.scalatest" % "scalatest_2.13" % "3.1.1" % "test"
libraryDependencies += "org.mockito" %% "mockito-scala" % "1.14.0"

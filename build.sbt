import com.scalapenos.sbt.prompt.SbtPrompt.autoImport._

//sonatypeSettings

promptTheme := ScalapenosTheme

name := "akvokolekta"

organization := "com.janschulte"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.7"

homepage := Some(url("http://www.janschulte.com"))

licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html"))

scmInfo := Some(ScmInfo(
    url("https://github.com/janschultecom/akvokolekta"),
    "scm:git:git@github.com/janschultecom/akvokolekta.git",
    Some("scm:git:git@github.com/janschultecom/akvokolekta.git")))

publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

pomExtra := (
  <developers>
    <developer>
      <id>janschultecom</id>
      <name>Jan Schulte</name>
      <url>www.janschulte.com</url>
    </developer>
  </developers>
)

pomIncludeRepository := { _ => false }

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots"),
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"
)

libraryDependencies ++= Seq(
  "org.scalanlp" %% "breeze" % "0.11.2",
  "com.yahoo.datasketches" % "sketches-core" % "0.2.2",
  "com.typesafe.akka" %% "akka-actor" % "2.4.2-RC1",
  "com.typesafe.akka" %% "akka-testkit" % "2.4.2-RC1",
  "com.typesafe.akka" %% "akka-stream" % "2.4.2-RC1",
  "org.scalacheck" %% "scalacheck" % "1.11.6" % "test",
  "org.specs2" %% "specs2" % "2.4.11" % "test"
)

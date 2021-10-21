import Dependencies.Versions

def crossScalacOptions(scalaVersion: String): Seq[String] =
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((3L, _)) =>
      Seq(
        "-source:3.0-migration",
        "-Xignore-scala2-macros"
      )
    case Some((2L, scalaMajor)) if scalaMajor >= 12 =>
      Seq(
        "-Ydelambdafy:method",
        "-target:jvm-1.8",
        "-Yrangepos",
        "-Ywarn-unused"
      )
  }

lazy val baseSettings = Seq(
  organization := "com.github.j5ik2o",
  homepage     := Some(url("https://github.com/j5ik2o/chronos-parser-scala")),
  licenses     := List("The MIT License" -> url("http://opensource.org/licenses/MIT")),
  developers := List(
    Developer(
      id = "j5ik2o",
      name = "Junichi Kato",
      email = "j5ik2o@gmail.com",
      url = url("https://blog.j5ik2o.me")
    )
  ),
  scalaVersion := Versions.scala213Version,
  // crossScalaVersions := Seq(Versions.scala213Version, Versions.scala3Version),
  scalacOptions ++= (
    Seq(
      "-unchecked",
      "-feature",
      "-deprecation",
      "-encoding",
      "UTF-8",
      "-language:_"
    ) ++ crossScalacOptions(scalaVersion.value)
  ),
  resolvers ++= Seq(
    Resolver.sonatypeRepo("snapshots"),
    Resolver.sonatypeRepo("releases"),
    "Seasar Repository" at "https://maven.seasar.org/maven2/"
  ),
  Test / publishArtifact   := false,
  Test / fork              := true,
  Test / parallelExecution := false,
  Compile / doc / sources := {
    val old = (Compile / doc / sources).value
    if (scalaVersion.value == Versions.scala3Version) {
      Nil
    } else {
      old
    }
  },
  semanticdbEnabled := true,
  semanticdbVersion := scalafixSemanticdb.revision,
  // Remove me when scalafix is stable and feature-complete on Scala 3
  ThisBuild / scalafixScalaBinaryVersion := (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, _)) => CrossVersion.binaryScalaVersion(scalaVersion.value)
    case _            => CrossVersion.binaryScalaVersion(Versions.scala212Version)
  })
)

val root = (project in file("."))
  .settings(baseSettings)
  .settings(
    name := "chronos-parser-scala",
    libraryDependencies ++= Seq(
      "com.github.j5ik2o" %% "intervals-scala" % "1.0.17",
      "org.scalatest"     %% "scalatest"       % "3.2.10" % Test
    ),
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "fastparse" % "2.3.3"
    ).map(_.cross(CrossVersion.for3Use2_13))
  )

// --- Custom commands
addCommandAlias("lint", ";scalafmtCheck;test:scalafmtCheck;scalafmtSbtCheck;scalafixAll --check")
addCommandAlias("fmt", ";scalafmtAll;scalafmtSbt;scalafix RemoveUnused")

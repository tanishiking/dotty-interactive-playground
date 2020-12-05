val dottyVersion = "3.0.0-M2"

lazy val root = project
  .in(file("."))
  .settings(
    name := "dotty-interactive-playground",
    version := "0.1.0",

    scalaVersion := dottyVersion,

    libraryDependencies ++= List(
      "org.scala-lang" %% "scala3-compiler" % scalaVersion.value,
      "org.scala-lang" %% "scala3-library" % scalaVersion.value,
      "org.scala-lang" %% "scala3-compiler" % scalaVersion.value,
      "io.get-coursier" % "interface" % "1.0.1",
      "com.lihaoyi" %% "pprint" % "0.6.0",
    )
  )

val dottyVersion = "3.1.1"

lazy val root = project
  .in(file("."))
  .settings(
    name := "dotty-interactive-playground",
    version := "0.1.0",

    scalaVersion := dottyVersion,

    libraryDependencies ++= List(
      "org.scala-lang" % "scala3-compiler_3" % scalaVersion.value,
      // "org.scala-lang" %% "scala3-library" % scalaVersion.value,
      "io.get-coursier" % "interface" % "1.0.7",
      // "com.lihaoyi" %% "pprint" % "0.7.3",
    )
  )

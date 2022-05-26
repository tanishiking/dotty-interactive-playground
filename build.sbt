val dottyVersion = "3.1.3-RC4"

lazy val root = project
  .in(file("."))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    name := "dotty-interactive-playground",
    version := "0.1.0",

    scalaVersion := dottyVersion,

    libraryDependencies ++= List(
      "org.scala-lang" % "scala3-compiler_3" % scalaVersion.value,
      // "org.scala-lang" %% "scala3-library" % scalaVersion.value,
      "io.get-coursier" % "interface" % "1.0.7",
    ),
    buildInfoKeys := Seq[BuildInfoKey](scalaVersion),
    buildInfoPackage := "playground"
  )

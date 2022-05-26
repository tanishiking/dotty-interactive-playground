package playground

import dotty.tools.dotc.interactive.{InteractiveDriver, Interactive, Completion}
import dotty.tools.dotc.util.{Spans, SourcePosition}
import dotty.tools.dotc.core.Contexts._

import coursierapi.{Fetch, Dependency}

import java.nio.file.Path
import java.net.URI

object Main:

  def main(args: Array[String]): Unit = {
    val driver = newDriver

    locally {
      val uri = new URI("file:///virtual")

      // driver.run invoke InteractiveCompiler with Mode.Interactive on the given source code.
      // [dotty/InteractiveCompiler.scala](https://github.com/lampepfl/dotty/blob/8059fcec9135e5672cc8a0359329258c6aaf837b/compiler/src/dotty/tools/dotc/interactive/InteractiveCompiler.scala)
      // and add the result to compilation unit.
      driver.run(uri, "object X { }")
    }

    /*
    // ===============================================
    // Feeding partial code and run completion
    // ===============================================
    locally {
      val sourceParital = "object X { def x = 1.toSt } "
      val uriPartial = new URI("file:///partial")
      val diag = driver.run(uriPartial, sourceParital)

      val pos = new SourcePosition(
        driver.openedFiles(uriPartial),
        Spans.Span(sourceParital.indexOf(".toSt") + ".toS".length) // run completion at "1.toS"
      )
      val completions = Completion.completions(pos)(using driver.currentCtx.fresh.setCompilationUnit(driver.compilationUnits.get(uriPartial).get))
      println(completions)
    }

    */

    locally {
      val sourceDefinition = "object Definition { def x    }"
      val uriDefinition = new URI("file:///def")
      driver.run(uriDefinition, sourceDefinition)
      given ctx: Context = driver.currentCtx

      val pos = new SourcePosition(driver.openedFiles(uriDefinition), Spans.Span(sourceDefinition.indexOf("def x") + "def x".length))
      val path = Interactive.pathTo(driver.openedTrees(uriDefinition), pos)
      path.foreach(println)

      // 3.1.3-RC2 or later
      // Literal(Constant(null))
      // DefDef(x,List(),TypeTree[TypeRef(ThisType(TypeRef(NoPrefix,module class scala)),class Null)],Literal(Constant(null)))
      // Template(DefDef(<init>,List(List()),TypeTree[TypeRef(ThisType(TypeRef(NoPrefix,module class scala)),class Unit)],EmptyTree),List(Apply(Select(New(TypeTree[TypeRef(ThisType(TypeRef(NoPrefix,module class lang)),class Object)]),<init>),List())),ValDef(_,SingletonTypeTree(Ident(Definition)),EmptyTree),List(DefDef(x,List(),TypeTree[TypeRef(ThisType(TypeRef(NoPrefix,module class scala)),class Null)],Literal(Constant(null)))))
      // TypeDef(Definition$,Template(DefDef(<init>,List(List()),TypeTree[TypeRef(ThisType(TypeRef(NoPrefix,module class scala)),class Unit)],EmptyTree),List(Apply(Select(New(TypeTree[TypeRef(ThisType(TypeRef(NoPrefix,module class lang)),class Object)]),<init>),List())),ValDef(_,SingletonTypeTree(Ident(Definition)),EmptyTree),List(DefDef(x,List(),TypeTree[TypeRef(ThisType(TypeRef(NoPrefix,module class scala)),class Null)],Literal(Constant(null))))))

      // 3.1.2
      // DefDef(x,List(),TypeTree[TypeRef(ThisType(TypeRef(NoPrefix,module class scala)),class Null)],Literal(Constant(null)))
      // Template(DefDef(<init>,List(List()),TypeTree[TypeRef(ThisType(TypeRef(NoPrefix,module class scala)),class Unit)],EmptyTree),List(Apply(Select(New(TypeTree[TypeRef(ThisType(TypeRef(NoPrefix,module class lang)),class Object)]),<init>),List())),ValDef(_,SingletonTypeTree(Ident(Definition)),EmptyTree),List(DefDef(x,List(),TypeTree[TypeRef(ThisType(TypeRef(NoPrefix,module class scala)),class Null)],Literal(Constant(null)))))
      // TypeDef(Definition$,Template(DefDef(<init>,List(List()),TypeTree[TypeRef(ThisType(TypeRef(NoPrefix,module class scala)),class Unit)],EmptyTree),List(Apply(Select(New(TypeTree[TypeRef(ThisType(TypeRef(NoPrefix,module class lang)),class Object)]),<init>),List())),ValDef(_,SingletonTypeTree(Ident(Definition)),EmptyTree),List(DefDef(x,List(),TypeTree[TypeRef(ThisType(TypeRef(NoPrefix,module class scala)),class Null)],Literal(Constant(null))))))
    }
  }


  def newDriver: InteractiveDriver =
    val fetch = Fetch.create()
    import scala.jdk.CollectionConverters._
    fetch.addDependencies(
      Dependency.of("org.scala-lang", "scala3-library_3", BuildInfo.scalaVersion)
    )
    val extraLibraries: Seq[Path] = fetch
      .fetch()
      .asScala
      .map(_.toPath())
      .toSeq

    // As the official standard library for scala 3.0 is the Scala 2.13 library, it looks like we need to
    // add scala 2.13.4 standard library to InteractiveDriver, to manipulate scala3 code (using stdlib)
    // > https://scalacenter.github.io/scala-3-migration-guide/docs/compatibility.html#the-scala-standard-library
    new InteractiveDriver(
      List(
        "-color:never",
        "-classpath",
        extraLibraries.mkString(java.io.File.pathSeparator)
      )
    )
  end newDriver


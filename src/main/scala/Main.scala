import dotty.tools.dotc.interactive.{InteractiveDriver, Interactive, Completion}
import dotty.tools.dotc.util.{Spans, SourcePosition}
import dotty.tools.dotc.core.Contexts._

import coursierapi.{Fetch, Dependency}

import java.nio.file.Path
import java.net.URI

object Main {

  def main(args: Array[String]): Unit = {
    val driver = newDriver

    locally {
      val uri = new URI("file:///virtual")

      // driver.run invoke InteractiveCompiler with Mode.Interactive on the given source code.
      // [dotty/InteractiveCompiler.scala](https://github.com/lampepfl/dotty/blob/8059fcec9135e5672cc8a0359329258c6aaf837b/compiler/src/dotty/tools/dotc/interactive/InteractiveCompiler.scala)
      // and add the result to compilation unit.
      driver.run(uri, "object X { }")

      // pprint.log(driver.openedFiles)
      // LinkedHashMap(file:///virtual -> /virtual)
    }

    // ===============================================
    // Feeding partial code and run completion
    // ===============================================
    locally {
      val sourceParital = "object X { def x = 1.toSt } "
      val uriPartial = new URI("file:///partial")
      val diag = driver.run(uriPartial, sourceParital)
      // pprint.log(diag)
      // List(
      //   class dotty.tools.dotc.reporting.Diagnostic$Error at /virtual:[19..21..25]: value toSt is not a member of Int - did you mean (1 : Int).toInt?,
      //   class dotty.tools.dotc.reporting.Diagnostic$Info at ?: 1 error found
      // )

      // pprint.log(driver.openedTrees(uriPartial))
      // Mode.Interactive makes parser error resillient using <error> symbol?
      // ...
      // preBody = List(
      //       DefDef(
      //         name = x,
      //         tparams = List(),
      //         vparamss = List(),
      //         tpt = TypeTree[dotty.tools.dotc.core.Types$PreviousErrorType@752771a8],
      //         preRhs = Select(qualifier = Literal(const = ( = 1)), name = toSt)
      //       )
      //     )

      val pos = new SourcePosition(
        driver.openedFiles(uriPartial),
        Spans.Span(sourceParital.indexOf(".toSt") + ".toS".length) // run completion at "1.toS"
      )
      val completions = Completion.completions(pos)(using driver.currentCtx.fresh.setCompilationUnit(driver.compilationUnits.get(uriPartial).get))
      println(completions)
      // (
      //   21,
      //   List(
      //     Completion(label = "toShort", description = "=> Short", symbols = List(method toShort)),
      //     Completion(label = "toString", description = "(): String", symbols = List(method toString))
      //   )
      // )
    }


    /*
    // ===============================================
    // Find definition
    // ===============================================
    locally {
      val sourceDefinition = "object Definition { def x = 1; val y = x + 1 }"
      val uriDefinition = new URI("file:///def")
      driver.run(uriDefinition, sourceDefinition)
      given ctx as Context = driver.currentCtx

      val pos = new SourcePosition(driver.openedFiles(uriDefinition), Spans.Span(sourceDefinition.indexOf("x + 1")))
      val path = Interactive.pathTo(driver.openedTrees(uriDefinition), pos)

      // Feeding path to the pos, and return definition's tree
      val definitions = Interactive.findDefinitions(path, pos, driver)
      // pprint.log(definitions)
      // Oops, it founds the definition of x from another compilation unit
      //
      // List(
      //   SourceTree(
      //     tree = DefDef(
      //       name = x,
      //       tparams = List(),
      //       vparamss = List(),
      //       tpt = TypeTree[dotty.tools.dotc.core.Types$PreviousErrorType@6b6b68d0],
      //       preRhs = Select(qualifier = Literal(const = ( = 1)), name = toSt)
      //     ),
      //     source = /partial
      //   ),
      //   SourceTree(
      //     tree = DefDef(
      //       name = x,
      //       tparams = List(),
      //       vparamss = List(),
      //       tpt = TypeTree[TypeRef(ThisType(TypeRef(NoPrefix,module class scala)),class Int)],
      //       preRhs = Literal(const = ( = 1))
      //     ),
      //     source = /def
      //   )
      // )
    }
    */
  }


  def newDriver: InteractiveDriver = {
    val fetch = Fetch.create()

    import scala.jdk.CollectionConverters._
    fetch.addDependencies(
      Dependency.of("org.scala-lang", "scala3-library_3", "3.1.1")
    )
    val extraLibraries: Seq[Path] = fetch
      .fetch()
      .asScala
      .map(_.toPath())
      .toSeq

    // pprint.log(extraLibraries)
    // List(
    //   /Users/tanishiking/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/scala3-library_3.0.0-M2/3.0.0-M2/scala3-library_3.0.0-M2-3.0.0-M2.jar,
    //   /Users/tanishiking/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/scala-library/2.13.4/scala-library-2.13.4.jar
    // )

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
  }
}

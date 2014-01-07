package com.typesafe.coffeescript;

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.Specification
import org.webjars.WebJarExtractor
import akka.util.Timeout
import scala.concurrent.duration._
import org.specs2.time.NoTimeConversions
import java.io.File
import scala.concurrent.Await
import _root_.sbt.IO
import scala.collection.immutable
import akka.actor.ActorSystem
import spray.json._
//import com.typesafe.jse.Trireme

@RunWith(classOf[JUnitRunner])
class CoffeeScriptCompilerSpec extends Specification with NoTimeConversions {

  implicit val duration = 15.seconds
  implicit val timeout = Timeout(duration)

  sequential

  private def withTempFiles[T](inputStrings: List[String], outputFilesNeeded: Int)(body: List[File] => T): (T, List[Option[String]]) = {
    IO.withTemporaryDirectory { tmpDir =>
      var nextId = 0
      def uniqueId: Int = {
        val returnValue = nextId
        nextId += 1
        returnValue
      }
      val inputFiles = inputStrings.map(_ => new File(tmpDir, s"input-$uniqueId"))
      val outputFiles = (0 until outputFilesNeeded).map(_ => new File(tmpDir, s"output-$uniqueId"))
      inputStrings.zip(inputFiles).foreach {
        case (string, file) => IO.write(file, string)
      }
      val tVal = body(List(tmpDir) ++ inputFiles ++ outputFiles)
      val outputStrings = outputFiles.map(f => if (f.exists()) Some(IO.read(f)) else None).to[List]
      (tVal, outputStrings)
    }
  }

  private def compile(args: CompileArgs): CompileResult = {
    implicit val actorSystem = ActorSystem()
    try {
      CoffeeScriptCompiler.compileFile(args)
    } finally {
      actorSystem.shutdown()
    }
  }

  "the CoffeeScript compiler" should {

    "compile a trivial file" in {
      withTempFiles(List("x = 1"), 1) {
        case List(tmpDir, csFile, jsFile) =>
          compile(CompileArgs(
            coffeeScriptInputFile = csFile,
            javaScriptOutputFile = jsFile,
            sourceMapOpts = None,
            bare = false,
            literate = false
          ))
      } match {
        case (compileResult, List(jsString)) =>
           compileResult must_== (CompileSuccess)
           jsString must_== Some(
            """|(function() {
               |  var x;
               |
               |  x = 1;
               |
               |}).call(this);
               |""".stripMargin('|'))
            }
    }

    "compile a bare file" in {
      withTempFiles(List("x = 1"), 1) {
        case List(tmpDir, csFile, jsFile) =>
          compile(CompileArgs(
            coffeeScriptInputFile = csFile,
            javaScriptOutputFile = jsFile,
            sourceMapOpts = None,
            bare = true,
            literate = false
          ))
      } match {
        case (compileResult, List(jsString)) =>
           compileResult must_== (CompileSuccess)
           jsString must_== Some(
            """|var x;
               |
               |x = 1;
               |""".stripMargin('|'))
      }
    }

    "compile a literate file" in {
      withTempFiles(List(
          """|Markdown markdown
             |
             |    x = 1
             |    y = 2
             |
             |More markdown.""".stripMargin('|')), 1) {
        case List(tmpDir, csFile, jsFile) =>
          compile(CompileArgs(
            coffeeScriptInputFile = csFile,
            javaScriptOutputFile = jsFile,
            sourceMapOpts = None,
            bare = false,
            literate = true
          ))
      } match {
        case (compileResult, List(jsString)) =>
          compileResult must_== (CompileSuccess)
          jsString must_== Some(
              """|(function() {
               |  var x, y;
               |
               |  x = 1;
               |
               |  y = 2;
               |
               |}).call(this);
               |""".stripMargin('|'))
      }
    }

  }

}
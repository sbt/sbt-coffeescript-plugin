import sbt.internal.inc.LoggedReporter

lazy val root = (project in file(".")).enablePlugins(SbtWeb)

WebKeys.reporter := {
  val logFile = target.value / "test-errors.log"
  new LoggedReporter(-1, new Logger {

    def trace(t: => Throwable): Unit = {}

    def success(message: => String): Unit = {}

    def log(level: Level.Value, message: => String): Unit = {
      if (level == Level.Error) {
        IO.append(logFile, message + "\n")
      }
    }
  })
}

val checkTestErrorLogContents = taskKey[Unit]("check that test log contents are correct")
checkTestErrorLogContents := {
  val contents = IO.read(target.value / "test-errors.log")
  if (!contents.endsWith("""/src/main/assets/a.coffee:0:0: unexpected %
                            |one error found
                            |""".stripMargin)) {
    sys.error(s"Unexpected contents: $contents")
  }
}
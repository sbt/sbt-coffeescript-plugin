organization := "com.typesafe.sbt"
name := "sbt-coffeescript"
libraryDependencies ++= Seq(
  "org.webjars" % "coffee-script-node" % "1.7.1",
  "org.webjars" % "mkdirp" % "0.3.5"
)
addSbtJsEngine("1.2.1")

enablePlugins(SbtPlugin)
organization := "com.typesafe.sbt"
name := "sbt-coffeescript"
scriptedBufferLog := false
scriptedLaunchOpts := { scriptedLaunchOpts.value ++
  Seq("-Dplugin.version=" + version.value)
}
libraryDependencies ++= Seq(
  "org.webjars.npm" % "coffeescript" % "2.5.1",
  "org.webjars" % "mkdirp" % "0.5.0"
)
addSbtJsEngine("1.2.3")

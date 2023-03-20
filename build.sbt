Global / onChangedBuildSource := ReloadOnSourceChanges

libraryDependencies ++= Seq(
  "org.tpolecat" %% "skunk-core" % "0.5.1",
  "org.tpolecat" %% "natchez-log" % "0.3.1",
  "org.typelevel" %% "log4cats-slf4j"   % "2.5.0",
  "ch.qos.logback" % "logback-classic" % "1.4.6"
)

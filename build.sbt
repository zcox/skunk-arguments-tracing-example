Global / onChangedBuildSource := ReloadOnSourceChanges

libraryDependencies ++= Seq(
  "org.tpolecat" %% "skunk-core" % "0.6.0",
  "org.tpolecat" %% "natchez-log" % "0.3.2",
  "org.typelevel" %% "log4cats-slf4j"   % "2.6.0",
  "ch.qos.logback" % "logback-classic" % "1.4.7"
)

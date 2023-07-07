Global / onChangedBuildSource := ReloadOnSourceChanges

scalaVersion := "2.13.10"

libraryDependencies ++= Seq(
  "org.tpolecat" %% "skunk-core" % "1.0-47e1ef0-20230705T191026Z-SNAPSHOT", // TODO update to 1.0.0-M1 when released
  "org.typelevel" %% "otel4s-java" % "0.2.1",
  "io.opentelemetry" % "opentelemetry-exporter-logging" % "1.27.0",
  "io.opentelemetry" % "opentelemetry-sdk-extension-autoconfigure" % "1.27.0-alpha" % Runtime,
  "org.typelevel" %% "log4cats-slf4j"   % "2.6.0",
  "ch.qos.logback" % "logback-classic" % "1.4.7"
)

run / fork := true
javaOptions ++= Seq(
  "-Dotel.java.global-autoconfigure.enabled=true",
  "-Dotel.service.name=jaeger-example",
  "-Dotel.metrics.exporter=none",
  "-Dotel.logs.exporter=none",
  "-Dotel.traces.exporter=logging"
)

name := "tala"

organization := "de.vorb"

version := "0.0.1"

scalaVersion := "2.11.4"

libraryDependencies ++= Seq(
    "org.mashupbots.socko" %% "socko-webserver" % "0.6.0",
    "io.spray" %%  "spray-json" % "1.3.1",
    "com.typesafe.slick" %% "slick" % "2.1.0",
    "org.slf4j" % "slf4j-nop" % "1.6.4",
    "com.mchange" % "c3p0" % "0.9.5-pre10",
    "org.xerial" % "sqlite-jdbc" % "3.8.7",
    "org.pegdown" % "pegdown" % "1.4.2",
    "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test"
)

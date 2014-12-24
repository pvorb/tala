name := "tala"

organization := "de.vorb"

version := "0.0.1"

scalaVersion := "2.11.4"

libraryDependencies ++= Seq(
    "org.mashupbots.socko" %% "socko-webserver" % "0.6.0",
    "com.google.guava" % "guava" % "18.0",
    "com.googlecode.json-simple" % "json-simple" % "1.1.1",
    "com.mchange" % "c3p0" % "0.9.5-pre10",
    "org.xerial" % "sqlite-jdbc" % "3.8.7",
    "com.googlecode.owasp-java-html-sanitizer" % "owasp-java-html-sanitizer" % "r239",
    "org.owasp.encoder" % "encoder" % "1.1.1",
    "org.scalatest" %% "scalatest" % "2.2.1" % Test,
    "org.apache.httpcomponents" % "httpclient" % "4.3.6" % Test
)

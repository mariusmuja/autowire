crossScalaVersions := Seq("2.10.4", "2.11.8", "2.12.0")

val autowire = crossProject.settings(
  organization := "de.daxten",

  version := "0.3.0",
  name := "autowire",
  scalaVersion := "2.11.8",
  autoCompilerPlugins := true,
  addCompilerPlugin("com.lihaoyi" %% "acyclic" % "0.1.5"),
  libraryDependencies ++= Seq(
    "com.lihaoyi" %% "acyclic" % "0.1.5" % "provided",
    "com.lihaoyi" %%% "utest" % "0.4.7" % "test",
    "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    "com.lihaoyi" %%% "upickle" % "0.4.4" % "test"
  ) ++ (
    if (!scalaVersion.value.startsWith("2.10.")) Nil
    else Seq(
      compilerPlugin("org.scalamacros" % s"paradise" % "2.0.0" cross CrossVersion.full),
      "org.scalamacros" %% s"quasiquotes" % "2.0.0"
    )
  ),
  testFrameworks += new TestFramework("utest.runner.Framework"),
  // Bintray
  publishArtifact in Test := false,
  bintrayReleaseOnPublish in ThisBuild := false,
  licenses in ThisBuild += ("MIT", url("http://opensource.org/licenses/MIT")),
  bintrayVcsUrl in ThisBuild := Some("git@github.com:daxten/autowire"),

    pomExtra :=
    <url>https://github.com/daxten/autowire</url>
      <licenses>
        <license>
          <name>MIT license</name>
          <url>http://www.opensource.org/licenses/mit-license.php</url>
        </license>
      </licenses>
      <scm>
        <url>git://github.com/daxten/autowire</url>
        <connection>scm:git://github.com/daxten/autowire.git</connection>
      </scm>
      <developers>
        <developer>
          <id>lihaoyi</id>
          <name>Li Haoyi</name>
          <url>https://github.com/lihaoyi</url>
        </developer>
        <developer>
          <id>daxten</id>
          <name>Alexej Haak</name>
          <url>https://github.com/daxten</url>
        </developer>
      </developers>
).jsSettings(
    resolvers ++= Seq(
      "bintray-alexander_myltsev" at "http://dl.bintray.com/content/alexander-myltsev/maven"
    ),
    scalaJSStage in Test := FullOptStage
).jvmSettings(
  resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/",
  libraryDependencies ++= Seq(
//    "org.scala-lang" %% "scala-pickling" % "0.9.1" % "test",
    "com.esotericsoftware.kryo" % "kryo" % "2.24.0" % "test"
//    "com.typesafe.play" %% "play-json" % "2.4.8" % "test"
  ),
  libraryDependencies ++= {
    if (!scalaVersion.value.startsWith("2.11.")) Nil
    else Seq(
      "org.scala-lang" %% "scala-pickling" % "0.9.1" % "test",
      "com.typesafe.play" %% "play-json" % "2.5.15" % "test"
    )
  }
)

lazy val autowireJS = autowire.js
lazy val autowireJVM = autowire.jvm

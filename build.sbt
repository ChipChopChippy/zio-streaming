val ZIOVersion = "1.0.1"

lazy val root = project
  .in(file("."))
  .settings(
    name := "zio-streaming",
    organization := "com.srfsoftware",
    scalaVersion := "2.13.3",
    initialCommands in Compile in console :=
      """|import zio._
         |import zio.console._
         |import zio.duration._
         |implicit class RunSyntax[R >: ZEnv, E, A](io: ZIO[R, E, A]){ def unsafeRun: A = Runtime.default.unsafeRun(io) }
    """.stripMargin
  )

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias(
    "check",
    "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck"
)

libraryDependencies ++= Seq(
    // ZIO
    "dev.zio" %% "zio"          % ZIOVersion,
    "dev.zio" %% "zio-streams"  % ZIOVersion,
    "dev.zio" %% "zio-test"     % ZIOVersion % "test",
    "dev.zio" %% "zio-test-sbt" % ZIOVersion % "test",
    "dev.zio" %% "zio-nio" % "1.0.0-RC9",
    "org.apache.httpcomponents" % "httpclient" % "4.5.12",
    "org.cryptacular" % "cryptacular" % "1.2.4"
)

testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))

scalacOptions in Compile in console := Seq(
    "-Ypartial-unification",
    "-language:higherKinds",
    "-language:existentials",
    "-Yno-adapted-args",
    "-Xsource:2.13",
    "-Yrepl-class-based",
    "-deprecation",
    "-encoding",
    "UTF-8",
    "-explaintypes",
    "-Yrangepos",
    "-feature",
    "-Xfuture",
    "-unchecked",
    "-Xlint:_,-type-parameter-shadow",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-opt-warnings",
    "-Ywarn-extra-implicit",
    "-Ywarn-unused:_,imports",
    "-Ywarn-unused:imports",
    "-opt:l:inline",
    "-opt-inline-from:<source>",
    "-Ypartial-unification",
    "-Yno-adapted-args",
    "-Ywarn-inaccessible",
    "-Ywarn-infer-any",
    "-Ywarn-nullary-override",
    "-Ywarn-nullary-unit"
)
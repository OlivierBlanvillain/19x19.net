scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats" % "0.6.0-M1",
  "com.github.mpilquist" %% "simulacrum" % "0.7.0",

  compilerPlugin("com.milessabin" % "si2712fix-plugin" % "1.2.0" cross CrossVersion.full),
  compilerPlugin("org.spire-math" %% "kind-projector" % "0.7.1"),
  compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),

  "org.scalatest" %% "scalatest" % "3.0.0-M7" % "test",
  "org.scalacheck" %% "scalacheck" % "1.12.5" % "test"
)

testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oDF")

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:experimental.macros",
  "-language:postfixOps",
  "-unchecked",
  // "-Xfatal-warnings",
  "-Xlint",
  "-Yinline-warnings",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Ywarn-unused-import",
  "-Xfuture"
)

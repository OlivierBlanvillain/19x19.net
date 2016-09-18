val scala         = "2.11.8"
val binding       = "9.0.1"
val boopickle     = "1.1.3"
val cats          = "0.6.0-M2"
val http4s        = "0.14.1a"
val kindProjector = "0.7.1"
val logback       = "1.1.7"
val paradise      = "2.1.0"
val scalacheck    = "1.13.1"
val scalaCss      = "0.4.1"
val scalatest     = "3.0.0-RC1"
val shapeless     = "2.3.1"
val simulacrum    = "0.7.0"

val scalacheckShapeless = "1.1.0-RC3"

lazy val root = project.in(file("."))
  .settings(settings: _*)
  .disablePlugins(spray.revolver.RevolverPlugin)
  .aggregate(sharedJVM, sharedJS, server, client)

lazy val shared = crossProject
  .crossType(CrossType.Pure)
  .jvmSettings(settings: _*)
  .jsSettings(jsSettings: _*)
  .disablePlugins(spray.revolver.RevolverPlugin)
  .settings(libraryDependencies ++= Seq(
    "me.chrons" %%% "boopickle" % boopickle,
    "org.typelevel" %%% "cats" % cats,
    "org.scalatest" %%% "scalatest" % scalatest,
    "org.scalacheck" %%% "scalacheck" % scalacheck,
    "com.chuusai" %%% "shapeless" % shapeless,
    "com.github.alexarchambault" %%% "scalacheck-shapeless_1.13" % scalacheckShapeless))

lazy val sharedJVM = shared.jvm
lazy val sharedJS = shared.js

lazy val server = project
  .settings(settings: _*)
  .dependsOn(sharedJVM)
  .settings(
    unmanagedResourceDirectories in Compile += baseDirectory.value / ".." / "static",
    assembly <<= assembly dependsOn (fullOptJS in Compile in client),
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % logback,
      "org.http4s" %% "http4s-dsl" % http4s,
      "org.http4s" %% "http4s-core" % http4s,
      "org.http4s" %% "http4s-blaze-server" % http4s,
      "org.http4s" %% "http4s-blaze-client" % http4s % "test"))

lazy val client = project
  .enablePlugins(ScalaJSPlugin)
  .settings(jsSettings: _*)
  .dependsOn(sharedJS)
  .disablePlugins(spray.revolver.RevolverPlugin)
  .settings(Seq(fullOptJS, fastOptJS, packageJSDependencies, packageScalaJSLauncher, packageMinifiedJSDependencies)
    .map(task => crossTarget in (Compile, task) := file("static/content/target")))
  .settings(libraryDependencies ++= Seq(
    "com.thoughtworks.binding" %%% "dom" % binding))

lazy val settings = Seq(
  scalaVersion := scala,
  resolvers += Resolver.sonatypeRepo("public"),
  testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oDF"),
  watchSources := watchSources.value.filterNot(_.getPath.contains("static")),
  scalacOptions := Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-language:implicitConversions",
    "-unchecked",
    // "-Xfatal-warnings",
    "-Xfuture",
    "-Xlint",
    "-Yinline-warnings",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard"),
  libraryDependencies ++= Seq(
    compilerPlugin("org.spire-math" %% "kind-projector" % kindProjector),
    compilerPlugin("org.scalamacros" % "paradise" % paradise cross CrossVersion.full))
) ++ warnUnusedImport

lazy val jsSettings = settings ++ Seq(
  emitSourceMaps := true,
  requiresDOM := true,
  scalaJSUseRhino := false,
  scalaJSStage in Test := FastOptStage,
  persistLauncher in Compile := true,
  persistLauncher in Test := false,
  artifactPath in (Compile, fastOptJS) :=
    ((crossTarget in (Compile, fastOptJS)).value /
      ((moduleName in fastOptJS).value + "-opt.js")))

lazy val warnUnusedImport = Seq(
  scalacOptions ++= (
    if (CrossVersion.partialVersion(scalaVersion.value) == Some((2, 10))) Nil
    else Seq("-Ywarn-unused-import")),
  scalacOptions in (Compile, console) ~= {_.filterNot("-Ywarn-unused-import" == _)},
  scalacOptions in (Test, console) <<= (scalacOptions in (Compile, console)))

addCommandAlias("validateJVM", ";sharedJVM/compile;server/compile;sharedJVM/test;server/test")
addCommandAlias("validateJS", ";sharedJS/compile;client/compile;sharedJS/test;client/test;client/fastOptJS")
addCommandAlias("validate", ";validateJS;validateJVM;server/assembly")
addCommandAlias("run", ";client/fastOptJS;server/reStart")

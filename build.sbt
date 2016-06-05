val scala         = "2.11.8"
val akkaHttp      = "2.4.5"
val akkaHttpTest  = "2.4.2-RC3"
val autowire      = "0.2.5"
val boopickle     = "1.1.3"
val cats          = "0.6.0-M2"
val kindProjector = "0.7.1"
val paradise      = "2.1.0"
val react         = "15.1.0"
val scalacheck    = "1.13.1"
val scalaCss      = "0.4.1"
val scalajsReact  = "0.11.1"
val scalatest     = "3.0.0-RC1"
val simulacrum    = "0.7.0"
val shapeless     = "2.3.1"
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
    "com.lihaoyi" %%% "autowire" % autowire,
    "me.chrons" %%% "boopickle" % boopickle,
    "org.typelevel" %%% "cats" % cats,
    "org.scalatest" %%% "scalatest" % scalatest,
    "org.scalacheck" %%% "scalacheck" % scalacheck,
    "com.chuusai" %%% "shapeless" % shapeless,
    "com.github.alexarchambault" %%% "scalacheck-shapeless_1.13" % scalacheckShapeless,
    compilerPlugin("com.github.mpilquist" %% "simulacrum" % simulacrum),
    compilerPlugin("org.spire-math" %% "kind-projector" % kindProjector),
    compilerPlugin("org.scalamacros" % "paradise" % paradise cross CrossVersion.full)))

lazy val sharedJVM = shared.jvm
lazy val sharedJS = shared.js

lazy val server = project
  .settings(settings: _*)
  .dependsOn(sharedJVM)
  .settings(
    unmanagedResourceDirectories in Compile += baseDirectory.value / ".." / "static",
    assembly <<= assembly dependsOn (fullOptJS in Compile in client),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http-experimental" % akkaHttp,
      "com.typesafe.akka" %% "akka-http-testkit-experimental" % akkaHttpTest % "test"))

lazy val client = project
  .enablePlugins(ScalaJSPlugin)
  .settings(jsSettings: _*)
  .dependsOn(sharedJS)
  .disablePlugins(spray.revolver.RevolverPlugin)
  .settings(Seq(fullOptJS, fastOptJS, packageJSDependencies, packageScalaJSLauncher, packageMinifiedJSDependencies)
    .map(task => crossTarget in (Compile, task) := file("static/content/target")))
  .settings(
    libraryDependencies ++= Seq(
      "com.github.japgolly.scalajs-react" %%% "core" % scalajsReact,
      "com.github.japgolly.scalajs-react" %%% "extra" % scalajsReact,
      "com.github.japgolly.scalajs-react" %%% "test" % scalajsReact % "test",
      "com.github.japgolly.scalacss" %%% "core" % scalaCss,
      "com.github.japgolly.scalacss" %%% "ext-react" % scalaCss),

    jsDependencies ++= Seq(
      "org.webjars.bower" % "react" % react
        /        "react-with-addons.js"
        minified "react-with-addons.min.js"
        commonJSName "React",

      "org.webjars.bower" % "react" % react
        /         "react-dom.js"
        minified  "react-dom.min.js"
        dependsOn "react-with-addons.js"
        commonJSName "ReactDOM",

      "org.webjars.bower" % "react" % react
        /         "react-dom-server.js"
        minified  "react-dom-server.min.js"
        dependsOn "react-dom.js"
        commonJSName "ReactDOMServer"))

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
    "-Xfatal-warnings",
    "-Xfuture",
    "-Xlint",
    "-Yinline-warnings",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-unused-import",
    "-Ywarn-value-discard"))

lazy val jsSettings = settings ++ Seq(
  emitSourceMaps := true,
  requiresDOM := true,
  scalaJSUseRhino := false,
  scalaJSStage in Test := FastOptStage,
  persistLauncher in Compile := true,
  persistLauncher in Test := false,
  skip in packageJSDependencies := false,
  artifactPath in (Compile, fastOptJS) :=
    ((crossTarget in (Compile, fastOptJS)).value / ((moduleName in fastOptJS).value + "-opt.js")))

addCommandAlias("validateJVM", ";sharedJVM/compile;server/compile;sharedJVM/test;server/test")
addCommandAlias("validateJS", ";sharedJS/compile;client/compile;sharedJS/test;client/test")
addCommandAlias("validate", ";validateJVM;validateJS")
addCommandAlias("run", ";client/fastOptJS;server/reStart")

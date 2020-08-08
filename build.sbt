val V = new {
  val Scala = "2.13.2"

  val cats = "2.1.1"
  val laminar = "0.9.1"
  val http4s = "0.21.3"
  val sttp = "2.2.0"
  val circe = "0.13.0"
  val decline = "1.0.0"
  val organiseImports = "0.4.0"
  val betterMonadicFor = "0.3.1"
  val utest = "0.7.4"
  val scalaJavaTime = "2.0.0"
}

val Dependencies = new {
  private val http4sModules =
    Seq("dsl", "blaze-client", "blaze-server", "circe").map("http4s-" + _)

  private val sttpModules = Seq("core", "circe")

  lazy val frontend = Seq(
    libraryDependencies ++=
      sttpModules.map("com.softwaremill.sttp.client" %%% _ % V.sttp) ++
        Seq("com.raquo" %%% "laminar" % V.laminar) ++
        Seq("com.lihaoyi" %%% "utest" % V.utest % Test) ++
        Seq("io.github.cquiroz" %%% "scala-java-time" % V.scalaJavaTime % Test) ++
        Seq("com.raquo" %%% "domtestutils" % "0.12.0" % Test)
  )

  lazy val backend = Seq(
    libraryDependencies ++=
      http4sModules.map("org.http4s" %% _ % V.http4s) ++
        Seq("com.monovore" %% "decline" % V.decline)
  )

  lazy val shared = new {
    val js = libraryDependencies += "io.circe" %%% "circe-generic" % V.circe
    val jvm = libraryDependencies += "io.circe" %% "circe-generic" % V.circe
  }
}

inThisBuild(
  Seq(
    scalafixDependencies += "com.github.liancheng" %% "organize-imports" % V.organiseImports,
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    scalafixScalaBinaryVersion := "2.13"
  )
)

lazy val root =
  (project in file(".")).aggregate(frontend, backend, shared.js, shared.jvm)

lazy val frontend = (project in file("modules/frontend"))
  .dependsOn(shared.js)
  .enablePlugins(ScalaJSPlugin)
  .settings(scalaJSUseMainModuleInitializer := true)
  .settings(
    Dependencies.frontend,
    Dependencies.shared.js,
    testFrameworks += new TestFramework("utest.runner.Framework"),
    jsEnv in Test := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv()
  )
  .settings(commonBuildSettings)

lazy val backend = (project in file("modules/backend"))
  .dependsOn(shared.jvm)
  .settings(Dependencies.backend)
  .settings(commonBuildSettings)
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)
  .settings(
    mappings in Universal += {
      val appJs = (frontend / Compile / fullOptJS).value.data
      appJs -> ("lib/prod.js")
    },
    javaOptions in Universal ++= Seq(
      "--port 8080",
      "--mode prod"
    ),
    packageName in Docker := "laminar-http4s-example"
  )

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("modules/shared"))
  .jvmSettings(Dependencies.shared.jvm)
  .jsSettings(Dependencies.shared.js)
  .jsSettings(commonBuildSettings)
  .jvmSettings(commonBuildSettings)

lazy val fastOptCompileCopy = taskKey[Unit]("")

val jsPath = "modules/backend/src/main/resources"

fastOptCompileCopy := {
  val source = (frontend / Compile / fastOptJS).value.data
  IO.copyFile(
    source,
    baseDirectory.value / jsPath / "dev.js"
  )
}

lazy val fullOptCompileCopy = taskKey[Unit]("")

fullOptCompileCopy := {
  val source = (frontend / Compile / fullOptJS).value.data
  IO.copyFile(
    source,
    baseDirectory.value / jsPath / "prod.js"
  )

}

lazy val commonBuildSettings: Seq[Def.Setting[_]] = Seq(
  scalaVersion := V.Scala,
  addCompilerPlugin("com.olegpy" %% "better-monadic-for" % V.betterMonadicFor),
  scalacOptions ++= Seq(
    "-Ywarn-unused"
  )
)

addCommandAlias("runDev", ";fastOptCompileCopy; backend/reStart --mode dev")
addCommandAlias("runProd", ";fullOptCompileCopy; backend/reStart --mode prod")

val scalafixRules = Seq(
  "OrganizeImports",
  "DisableSyntax",
  "LeakingImplicitClassVal",
  "ProcedureSyntax",
  "NoValInForComprehension"
).mkString(" ")

val CICommands = Seq(
  "clean",
  "backend/compile",
  "backend/test",
  "frontend/compile",
  "frontend/fastOptJS",
  "frontend/test",
  "scalafmtCheckAll",
  s"scalafix --check $scalafixRules"
).mkString(";")

val PrepareCICommands = Seq(
  s"compile:scalafix --rules $scalafixRules",
  s"test:scalafix --rules $scalafixRules",
  "test:scalafmtAll",
  "compile:scalafmtAll",
  "scalafmtSbt"
).mkString(";")

addCommandAlias("ci", CICommands)

addCommandAlias("preCI", PrepareCICommands)

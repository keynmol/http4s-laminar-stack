val V = new {
  val cats = "2.1.1"
  val laminar = "0.9.1"
  val http4s = "0.21.3"
  val sttp = "2.2.0"
  val circe = "0.13.0"
  val decline = "1.0.0"
  val organiseImports = "0.4.0"
  val betterMonadicFor = "0.3.1"
}

val M = new {
  val http4sModules = Seq("dsl", "blaze-client", "blaze-server", "circe")
  val sttpModules = Seq("core", "circe")
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
    libraryDependencies ++= Seq(
      "com.raquo" %%% "laminar" % V.laminar
    ),
    libraryDependencies ++= M.sttpModules
      .map("com.softwaremill.sttp.client" %%% _ % V.sttp)
  )
  .settings(commonBuildSettings)
  .settings(
    copyFrontendFastOpt := {
      (fastOptJS in Compile).value.data
    },
    copyFrontendFullOpt := {
      (fullOptJS in Compile).value.data
    }
  )

lazy val backend = (project in file("modules/backend"))
  .dependsOn(shared.jvm)
  .settings(
    libraryDependencies ++= M.http4sModules
      .map(module => "org.http4s" %% s"http4s-$module" % V.http4s),
    libraryDependencies += "com.monovore" %% "decline" % V.decline
  )
  .settings(commonBuildSettings)
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)
  .settings(
    mappings in Universal += {
      val appJs = (copyFrontendFullOpt in frontend).value
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
  .jvmSettings(
    libraryDependencies += "io.circe" %% "circe-generic" % V.circe
  )
  .jsSettings(
    libraryDependencies += "io.circe" %%% "circe-generic" % V.circe
  )
  .jsSettings(commonBuildSettings)
  .jvmSettings(commonBuildSettings)

val copyFrontendFastOpt = taskKey[File]("bla")
val copyFrontendFullOpt = taskKey[File]("bla")

lazy val fastOptCompileCopy = taskKey[Unit]("bla")

val jsPath = "modules/backend/src/main/resources"

fastOptCompileCopy := {
  val source = (copyFrontendFastOpt in frontend).value
  IO.copyFile(
    source,
    baseDirectory.value / jsPath / "dev.js"
  )
}

lazy val fullOptCompileCopy = taskKey[Unit]("bla")

fullOptCompileCopy := {
  val source = (copyFrontendFullOpt in frontend).value
  IO.copyFile(
    source,
    baseDirectory.value / jsPath / "prod.js"
  )

}

lazy val commonBuildSettings: Seq[Def.Setting[_]] = Seq(
  scalaVersion := "2.13.2",
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
  s"scalafix --check $scalafixRules"
).mkString(";")

addCommandAlias("ci", CICommands)

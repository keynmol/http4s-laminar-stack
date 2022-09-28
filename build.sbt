val V = new {
  val Scala = "3.2.0"

  val laminar = "0.14.5"

  val http4s = "0.23.15"

  val http4sDom = "0.2.3"

  val circe = "0.14.3"

  val decline = "2.1.0"

  val organiseImports = "0.6.0"

  val weaver = "0.8.0"
}

val Dependencies = new {
  private val http4sModules =
    Seq("dsl", "ember-client", "ember-server", "circe").map("http4s-" + _)

  private val sttpModules = Seq("core", "circe")

  lazy val frontend = Seq(
    libraryDependencies ++=
      Seq(
        "org.http4s" %%% "http4s-client" % V.http4s,
        "org.http4s" %%% "http4s-circe"  % V.http4s,
        "org.http4s" %%% "http4s-dom"    % V.http4sDom,
        "com.raquo"  %%% "laminar"       % V.laminar
      )
  )

  lazy val backend = Seq(
    libraryDependencies ++=
      http4sModules.map("org.http4s" %% _ % V.http4s) ++
        Seq("com.monovore" %% "decline" % V.decline)
  )

  lazy val shared = Def.settings(
    libraryDependencies += "io.circe" %%% "circe-core" % V.circe
  )

  lazy val tests = Def.settings(
    libraryDependencies += "com.disneystreaming" %%% "weaver-cats" % V.weaver % Test,
    testFrameworks += new TestFramework("weaver.framework.CatsEffect")
  )
}

inThisBuild(
  Seq(
    scalafixDependencies += "com.github.liancheng" %% "organize-imports" % V.organiseImports,
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision
  )
)

lazy val root =
  project.in(file(".")).aggregate(frontend, backend, shared.js, shared.jvm)

lazy val frontend = project
  .in(file("modules/frontend"))
  .dependsOn(shared.js)
  .enablePlugins(ScalaJSPlugin)
  .settings(scalaJSUseMainModuleInitializer := true)
  .settings(
    Dependencies.frontend,
    Dependencies.tests,
    Test / jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv()
  )
  .settings(commonBuildSettings)

lazy val backend = project
  .in(file("modules/backend"))
  .dependsOn(shared.jvm)
  .settings(Dependencies.backend)
  .settings(Dependencies.tests)
  .settings(commonBuildSettings)
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)
  .settings(
    Test / fork := true,
    Docker / mappings += {
      val appJs = (frontend / Compile / fullOptJS).value.data
      appJs -> "/opt/docker/resources/prod.js"
    },
    Universal / javaOptions ++= Seq(
      "--port 8080",
      "--mode prod"
    ),
    Docker / packageName := "laminar-http4s-example"
  )

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("modules/shared"))
  .jvmSettings(Dependencies.shared)
  .jsSettings(Dependencies.shared)
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

lazy val commonBuildSettings: Seq[Def.Setting[?]] = Seq(
  scalaVersion := V.Scala
)

addCommandAlias("runDev", ";fastOptCompileCopy; backend/reStart --mode dev")
addCommandAlias("runProd", ";fullOptCompileCopy; backend/reStart --mode prod")

val scalafixRules = Seq(
  "OrganizeImports",
  "DisableSyntax",
  "LeakingImplicitClassVal",
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
  "scalafmtAll",
  "scalafmtSbt",
  s"scalafix $scalafixRules"
).mkString(";")

addCommandAlias("ci", CICommands)

addCommandAlias("preCI", PrepareCICommands)

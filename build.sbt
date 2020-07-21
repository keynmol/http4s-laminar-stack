val V = new {
  val cats = "2.1.1"
  val laminar = "0.9.1"
  val http4s = "0.21.3"
  val sttp = "2.2.0"
  val circe = "0.13.0"
}

val M = new {
  val http4sModules = Seq("dsl", "blaze-client", "blaze-server", "circe")
}

lazy val root =
  (project in file(".")).aggregate(frontend, backend, shared.js, shared.jvm)

lazy val frontend = (project in file("frontend"))
  .dependsOn(shared.js)
  .enablePlugins(ScalaJSPlugin)
  .settings(scalaJSUseMainModuleInitializer := true)
  .settings(
    libraryDependencies ++= Seq(
      "com.raquo" %%% "laminar" % V.laminar,
      "com.softwaremill.sttp.client" %%% "core" % V.sttp,
      "com.softwaremill.sttp.client" %%% "circe" % V.sttp
    )
  )
  .settings(commonBuildSettings)
  .settings(
    copyFrontendFastOpt := {
      (fastOptJS in Compile).value.data
    }
  )

lazy val backend = (project in file("backend"))
  .dependsOn(shared.jvm)
  .settings(
    libraryDependencies ++= M.http4sModules
      .map(module => "org.http4s" %% s"http4s-$module" % V.http4s)
  )
  .settings(commonBuildSettings)

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("shared"))
  .jvmSettings(
    libraryDependencies += "io.circe" %% "circe-generic" % V.circe
  )
  .jsSettings(
    libraryDependencies += "io.circe" %%% "circe-generic" % V.circe
  )
  .jsSettings(commonBuildSettings)
  .jvmSettings(commonBuildSettings)

val copyFrontendFastOpt = taskKey[File]("bla")

lazy val fastOptCompileCopy = taskKey[Unit]("bla")

fastOptCompileCopy := {
  val source = (copyFrontendFastOpt in frontend).value 
  IO.copyFile(
    source,
    baseDirectory.value / "backend/src/main/resources" / "dev.js"
  )

}

lazy val commonBuildSettings: Seq[Def.Setting[_]] = Seq(
  scalaVersion := "2.13.2",
  addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
)

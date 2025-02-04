addSbtPlugin("org.scala-js"       % "sbt-scalajs"              % "1.13.2")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")
addSbtPlugin("io.spray"           % "sbt-revolver"             % "0.9.1")

addSbtPlugin("org.scalameta"    % "sbt-scalafmt"        % "2.5.4")
addSbtPlugin("com.github.sbt"   % "sbt-native-packager" % "1.11.1")
addSbtPlugin("ch.epfl.scala"    % "sbt-scalafix"        % "0.10.4")
addSbtPlugin("com.timushev.sbt" % "sbt-updates"         % "0.6.4")

libraryDependencies += "org.scala-js" %% "scalajs-env-jsdom-nodejs" % "1.1.0"

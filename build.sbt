name := "datadog-client"
organization := "com.udacity"
scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
    "org.scalaz"          %% "scalaz-core"       % "7.1.0"
  , "io.argonaut"         %% "argonaut"          % "6.1-M4"
)

libraryDependencies ++= Seq( // test
    "org.scalatra"      %% "scalatra"          % "2.3.1"
  , "org.eclipse.jetty" %  "jetty-webapp"      % "9.1.0.v20131115"
  , "org.eclipse.jetty" %  "jetty-plus"        % "9.1.0.v20131115"
  , "javax.servlet"     %  "javax.servlet-api" % "3.1.0"
  , "org.scalatest"     %% "scalatest"         % "2.2.1"
  , "org.apache.httpcomponents" % "httpclient" % "4.5.1"
) map { x => x % "test" }

dependencyOverrides := Set(
    "org.scala-lang" % "scala-library" % scalaVersion.value
  , "org.scala-lang" % "scala-reflect" % scalaVersion.value
  , "org.scala-lang" % "scala-compiler" % scalaVersion.value
)

ThisBuild / scalafmtOnCompile := true

// Scalafix
inThisBuild(
  List(
    // scalaVersion := "2.13.3",
    semanticdbEnabled := true,
    semanticdbVersion := "4.9.6"
  )
)

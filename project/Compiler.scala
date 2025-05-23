import sbt.Keys._
import sbt._

object Compiler {

  private val silencerVer = Def.setting[String] {
    "1.7.19"
  }

  lazy val settings = Seq(
    Compile / unmanagedSourceDirectories += {
      val base = (Compile / sourceDirectory).value

      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if n < 13 => base / "scala-2.13-"
        case _                      => base / "scala-2.13+"
      }
    },
    Test / unmanagedSourceDirectories += {
      val base = (Test / sourceDirectory).value

      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if n < 13 => base / "scala-2.13-"
        case _                      => base / "scala-2.13+"
      }
    },
    scalacOptions ++= Seq(
      "-encoding",
      "UTF-8",
      "-unchecked",
      "-deprecation",
      "-feature",
      "-Xfatal-warnings",
      "-language:higherKinds"
    ),
    scalacOptions ++= {
      if (scalaBinaryVersion.value startsWith "2.") {
        Seq(
          "-Xlint",
          "-g:vars"
        )
      } else Seq.empty
    },
    scalacOptions ++= {
      val sv = scalaBinaryVersion.value

      if (sv == "2.12") {
        Seq(
          "-target:jvm-1.8",
          "-Xmax-classfile-name",
          "128",
          "-Ywarn-numeric-widen",
          "-Ywarn-dead-code",
          "-Ywarn-value-discard",
          "-Ywarn-infer-any",
          "-Ywarn-unused",
          "-Ywarn-unused-import",
          "-Xlint:missing-interpolator",
          "-Ywarn-macros:after"
        )
      } else if (sv == "2.11") {
        Seq(
          "-target:jvm-1.8",
          "-Xmax-classfile-name",
          "128",
          "-Yopt:_",
          "-Ydead-code",
          "-Yclosure-elim",
          "-Yconst-opt"
        )
      } else if (sv == "2.13") {
        Seq(
          "-release",
          "8",
          "-explaintypes",
          "-Werror",
          "-Wnumeric-widen",
          "-Wdead-code",
          "-Wvalue-discard",
          "-Wextra-implicit",
          "-Wmacros:after",
          "-Wunused"
        )
      } else {
        Seq("-Wunused:all", "-language:implicitConversions")
      }
    },
    scalacOptions ++= {
      val sv = scalaBinaryVersion.value

      if (sv == "2.13" || sv == "2.12") {
        Seq(
          "-Wconf:src=.*ReactiveMongoModule\\.scala&msg=.*var\\ .*is\\ never\\ updated.*:s"
        )
      } else if (sv startsWith "3") {
        Seq(
          "-Wconf:msg=.*with\\ as\\ a\\ type\\ operator.*:s",
          "-Wconf:msg=.*is\\ not\\ declared\\ infix.*:s",
          "-Wconf:msg=.*is\\ deprecated\\ for\\ wildcard\\ arguments\\ of\\ types.*:s",
          "-Wconf:msg=.*syntax\\ .*function.*\\ is\\ no\\ longer\\ supported.*:s",
          "-Wconf:msg=.*unset\\ private\\ variable.*:s",
          "-Wconf:msg=.*=\\ uninitialized.*:s",
          "-Wconf:msg=.*unused.*:s"
        )
      } else {
        Seq.empty
      }
    },
    Compile / doc / scalacOptions := (Test / scalacOptions).value,
    Compile / console / scalacOptions ~= {
      _.filterNot { opt => opt.startsWith("-X") || opt.startsWith("-Y") }
    },
    Test / console / scalacOptions ~= {
      _.filterNot { opt => opt.startsWith("-X") || opt.startsWith("-Y") }
    },
    libraryDependencies ++= {
      if (scalaBinaryVersion.value != "3") {
        Seq(
          compilerPlugin(
            ("com.github.ghik" %% "silencer-plugin" % silencerVer.value)
              .cross(CrossVersion.full)
          ),
          ("com.github.ghik" %% "silencer-lib" % silencerVer.value % Provided)
            .cross(CrossVersion.full)
        )
      } else {
        Seq.empty
      }
    },
    // Mock silencer for Scala3
    Test / doc / scalacOptions ++= List("-skip-packages", "com.github.ghik"),
    Compile / packageBin / mappings ~= {
      _.filter { case (_, path) => !path.startsWith("com/github/ghik") }
    },
    Compile / packageSrc / mappings ~= {
      _.filter { case (_, path) => path != "silent.scala" }
    }
  )
}

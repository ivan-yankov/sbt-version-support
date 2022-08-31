package yankov.sbt.plugin

import sbt.{Def, *}

import java.nio.file.{Files, Paths}

object SbtVersionSupportPlugin extends AutoPlugin {
  object autoImport {
    val incrementVersion = taskKey[Unit]("increment version")
    val versionFile = settingKey[String]("version file")
    val increaseVersion = settingKey[String => String]("increment version implementation")
    val readVersion = settingKey[() => String]("read current version")
  }

  import autoImport.*

  private val defaultSettings: Seq[Def.Setting[?]] = Seq(
    versionFile := "version.txt",

    increaseVersion := {
      version => (version.toInt + 1).toString
    },

    readVersion := {
      Files.readString(Paths.get(versionFile.value)).trim
    },

    incrementVersion := {
      def writeVersion(v: String): Unit = Files.writeString(Paths.get(versionFile.value), v + "\n")

      writeVersion(increaseVersion.value(readVersion.value()))
    }
  )

  override def trigger = allRequirements

  override lazy val projectSettings: Seq[Def.Setting[?]] = defaultSettings

  override lazy val buildSettings: Seq[Def.Setting[?]] = defaultSettings
}

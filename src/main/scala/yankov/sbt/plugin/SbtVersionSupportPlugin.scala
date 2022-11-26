package yankov.sbt.plugin

import sbt.{Def, *}

import java.nio.file.{Files, Paths}

object SbtVersionSupportPlugin extends AutoPlugin {
  private val latest = "latest"

  case class Version(major: Int, minor: Int) {
    def incMajor: Version = Version(major + 1, 0)

    def incMinor: Version = Version(major, minor + 1)
  }

  def writeVersion(fileName: String, version: String): Unit = Files.writeString(Paths.get(fileName), version + "\n")

  def loadVersion(fileName: String): String = Files.readString(Paths.get(fileName)).trim

  def parseVersion(s: String): Either[String, Version] = {
    if (latest.equals(s)) Left(latest)
    else {
      try {
        val v = s.split(".")
        Right(Version(v(0).toInt, v(1).toInt))
      } catch {
        case _: Exception => Right(Version(1, 0))
      }
    }
  }

  def printVersion(version: Version): String = version.major + "." + version.minor

  def incVersion(fileName: String, major: Boolean): Unit = {
    parseVersion(loadVersion(fileName)) match {
      case Left(value) => writeVersion(fileName, value)
      case Right(value) if major => writeVersion(fileName, printVersion(value.incMajor))
      case Right(value) => writeVersion(fileName, printVersion(value.incMinor))
    }
  }

  object autoImport {
    val incrementMajorVersion = taskKey[Unit]("increment major version")
    val incrementMinorVersion = taskKey[Unit]("increment minor version")
    val versionFile = settingKey[String]("version file")
    val readVersion = settingKey[() => String]("read current version")
  }

  import autoImport.*

  private val defaultSettings: Seq[Def.Setting[?]] = Seq(
    versionFile := "version.txt",

    readVersion := {
      () => loadVersion(versionFile.value)
    },

    incrementMajorVersion := {
      incVersion(versionFile.value, major = true)
    },

    incrementMinorVersion := {
      incVersion(versionFile.value, major = false)
    }
  )

  override def trigger = allRequirements

  override lazy val projectSettings: Seq[Def.Setting[?]] = defaultSettings

  override lazy val buildSettings: Seq[Def.Setting[?]] = defaultSettings
}

package top.lanscarlos.vulpecula.module.bacikal.extension

import taboolib.module.configuration.Configuration
import java.io.File
import java.util.jar.JarFile

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.extension
 *
 * @author Lanscarlos
 * @since 2025/10/11
 */
class Artifact(val file: File) : Comparable<Artifact> {

    val name: String

    val version: String

    val majorVersion: Int

    val minorVersion: Int

    val patchVersion: Int

    init {
        require(file.extension == "jar") { "${file.path} is not a jar" }
        require(file.exists()) { "${file.path} does not exist" }
        require(file.isFile) { "${file.path} is not a file" }
        require(file.canRead()) { "${file.path} can not be read" }

        JarFile(file).use { jarFile ->
            val jarEntry = jarFile.getJarEntry("plugin.yml") ?: error("空的 plugin.yml")
            val inputStream = jarFile.getInputStream(jarEntry)
            val config = Configuration.loadFromInputStream(inputStream)
            name = config.getString("name") ?: error("Invalid plugin.yml")
            version = config.getString("version") ?: error("Invalid plugin.yml")
            val array = version.split('.')
            majorVersion = array.getOrNull(0)?.toInt() ?: 0
            minorVersion = array.getOrNull(1)?.toInt() ?: 0
            patchVersion = array.getOrNull(2)?.toInt() ?: 0
        }
    }

    override fun compareTo(other: Artifact): Int {
        if (majorVersion != other.majorVersion) {
            return majorVersion.compareTo(other.majorVersion)
        }
        if (minorVersion != other.minorVersion) {
            return minorVersion.compareTo(other.minorVersion)
        }
        return patchVersion.compareTo(other.patchVersion)
    }

}
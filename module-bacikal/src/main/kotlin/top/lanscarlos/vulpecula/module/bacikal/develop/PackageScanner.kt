package top.lanscarlos.vulpecula.module.bacikal.develop

import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitorHandler.getClasses
import taboolib.common.platform.Awake
import taboolib.common.platform.function.info
import java.io.File

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.develop
 *
 * @author Lanscarlos
 * @since 2025/9/7
 */
object PackageScanner {

    @Awake(LifeCycle.ACTIVE)
    fun onActive() {
        try {
            val packageName = Entity::class.java.`package`.name
            info("尝试获取实体包 $packageName 下的所有类")
            val classes = getClassesInPackage(packageName)
            for (clazz in classes) {
                info("<检索> ${clazz.name}")
            }
            if (classes.isEmpty()) {
                info("未检索到任何包")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getClassesInPackage(packageName: String): List<Class<*>> {
        val classLoader = Bukkit::class.java.classLoader
        val path = packageName.replace('.', '/')
        info("path >> $path")
        val url = classLoader.getResource(path)!!
        info("url >> ${url.file}")
        val directory = File(url.path)
        return directory.walk()
            .onEach { info("scan >> ${it.absolutePath}") }
            .filter { it.isFile && it.name.endsWith(".class") }
            .map { classFile ->
                val className = "$packageName." +
                        classFile.relativeTo(directory)
                            .toString()
                            .removeSuffix(".class")
                            .replace(File.separatorChar, '.')
                Class.forName(className)
            }
            .toList()
    }

}
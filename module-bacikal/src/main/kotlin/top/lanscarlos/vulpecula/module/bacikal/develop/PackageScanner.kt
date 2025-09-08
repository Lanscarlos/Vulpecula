package top.lanscarlos.vulpecula.module.bacikal.develop

import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitorHandler.getClasses
import taboolib.common.platform.Awake
import taboolib.common.platform.function.info
import java.io.File
import java.util.jar.JarFile
import java.util.stream.Collectors

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.develop
 *
 * @author Lanscarlos
 * @since 2025/9/7
 */
object PackageScanner {

//    @Awake(LifeCycle.ACTIVE)
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
        val url = classLoader.getResource(path)!!
        val jarPath = url.path.substring(5, url.path.indexOf("!"))
        val jarFile = JarFile(jarPath)
        val classes = jarFile.stream()
            .filter {
                it.name.startsWith(path) && it.name.endsWith(".class") && !it.name.contains("$")
            }
            .map { entry ->
                entry.name.replace('/', '.').replace(".class", "")
            }
            .map(Class<*>::forName)
            .collect(Collectors.toList())

        return classes
    }

}
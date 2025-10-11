package top.lanscarlos.vulpecula.module.bacikal.extension

import taboolib.common.ClassAppender
import taboolib.common.LifeCycle
import taboolib.common.TabooLib
import taboolib.common.inject.ClassVisitor
import taboolib.common.io.getClasses
import taboolib.common.platform.Awake
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.event.EventBus
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.getDataFolder
import taboolib.library.reflex.ReflexClass
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import top.lanscarlos.vulpecula.common.core.command.CommandScanner
import java.io.File
import java.io.FileOutputStream
import java.util.jar.JarFile

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.extension
 *
 * @author Lanscarlos
 * @since 2025/10/11
 */
class ExternalExtension(artifact: Artifact) : Extension {

    val classes: Map<String, ReflexClass>

    override val name: String = artifact.name

    override val version: String = artifact.version

    override val authors: List<String>

    val file: File = artifact.file

    val config: Configuration

    init {
        // 读取包体注册信息
        JarFile(artifact.file).use { jarFile ->
            val jarEntry = jarFile.getJarEntry("plugin.yml") ?: error("File plugin.yml not found in ${file.path}")
            val inputStream = jarFile.getInputStream(jarEntry)
            val registry = Configuration.loadFromInputStream(inputStream, Type.YAML)
            authors = registry.getStringList("authors")
            config = initActionConfig(jarFile)
        }

        // 载入包体
        ClassAppender.addPath(file.toPath(), false, false)
        classes = file.toURI().toURL().getClasses()

        // 处理包体信息
        scanAwakeMethod()
        scanCommandMethod()
        TabooLib.registerLifeCycleTask(LifeCycle.ENABLE, 0, ::scanEventMethod)
    }

    fun reload() {
        config.reload()
    }

    private fun scanAwakeMethod() {
        for (owner in classes.values) {
            for (method in owner.structure.methods) {
                if (!method.isAnnotationPresent(Awake::class.java)) {
                    continue
                }
                val annotation = method.getAnnotation(Awake::class.java)
                val lifeCycle = annotation.enum("value", LifeCycle.CONST)
                val instance = owner.getInstance()
                TabooLib.registerLifeCycleTask(lifeCycle, 0) {
                    if (instance != null) {
                        method.invoke(instance)
                    } else {
                        method.invokeStatic()
                    }
                }
            }
        }
    }

    private fun scanCommandMethod() {
        for (owner in classes.values) {
            for (field in owner.structure.fields) {
                if (!field.isAnnotationPresent(CommandBody::class.java)) {
                    continue
                }
                CommandScanner.visit(field, owner)
            }
        }
    }

    private fun scanEventMethod() {
        for (owner in classes.values) {
            for (method in owner.structure.methods) {
                if (!method.isAnnotationPresent(SubscribeEvent::class.java)) {
                    continue
                }
                eventBus.visit(method, owner)
            }
        }
    }

    private fun initActionConfig(jarFile: JarFile): Configuration {
        val file = File(getDataFolder(), "config/${name}.yml")
        if (file.exists()) {
            return Configuration.loadFromFile(file)
        }
        file.parentFile.mkdirs()

        // 获取输入流
        val inputStream = jarFile.getJarEntry("config.yml")?.let(jarFile::getInputStream)
            ?: return Configuration.empty()

        // 输出文件
        inputStream.use {
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        return Configuration.loadFromInputStream(inputStream)
    }

    companion object {

        val eventBus: EventBus by lazy {
            ClassVisitor.findInstance(ReflexClass.of(EventBus::class.java)) as EventBus
        }

    }

}
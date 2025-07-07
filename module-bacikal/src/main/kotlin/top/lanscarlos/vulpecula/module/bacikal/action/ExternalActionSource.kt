package top.lanscarlos.vulpecula.module.bacikal.action

import taboolib.common.LifeCycle
import taboolib.common.TabooLib
import taboolib.common.inject.ClassVisitor
import taboolib.common.platform.Awake
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.event.EventBus
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.getDataFolder
import taboolib.library.reflex.ReflexClass
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import top.lanscarlos.vulpecula.common.core.command.CommandScanner
import top.lanscarlos.vulpecula.common.core.utils.asLang
import top.lanscarlos.vulpecula.module.bacikal.BacikalRegistry
import java.io.File
import java.io.FileOutputStream

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.action
 *
 * @author Lanscarlos
 * @since 2025/7/2 10:12
 */
class ExternalActionSource(val classes: Map<String, ReflexClass>, resources: Map<String, ByteArray>) : ActionSource {

    override val name: String

    override val version: String

    override val authors: List<String>

    val config: Configuration

    init {
        val registry = initActionRegistry(resources)
        name = registry.getString("name") ?: "UNKNOWN_NAME"
        version = registry.getString("version") ?: "UNKNOWN_VERSION"
        authors = registry.getStringList("authors")
        config = initActionConfig(resources)
        BacikalRegistry.registerActionSource(this)
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

    private fun initActionConfig(resources: Map<String, ByteArray>): Configuration {
        val file = File(getDataFolder(), "config/${name}.yml")
        if (file.exists()) {
            return Configuration.loadFromFile(file)
        }
        file.parentFile.mkdirs()
        val inputStream = resources.entries.find { it.key == "config.yml" }?.value?.inputStream()
            ?: return Configuration.empty()
        // 输出文件
        inputStream.use {
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return Configuration.loadFromInputStream(inputStream)
    }

    private fun initActionRegistry(resources: Map<String, ByteArray>): Configuration {
        val inputStream = resources.entries.find { it.key == "plugin.yml" }?.value?.inputStream()
            ?: error(asLang("Registry not found."))
        return Configuration.loadFromInputStream(inputStream, Type.YAML)
    }

    companion object {

        val eventBus: EventBus by lazy {
            ClassVisitor.findInstance(ReflexClass.of(EventBus::class.java)) as EventBus
        }

    }

}
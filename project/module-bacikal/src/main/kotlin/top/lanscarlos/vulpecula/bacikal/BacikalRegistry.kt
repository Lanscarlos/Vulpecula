package top.lanscarlos.vulpecula.bacikal

import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitor
import taboolib.common.io.taboolibPath
import taboolib.common.platform.Awake
import taboolib.common.platform.function.getOpenContainers
import taboolib.common.platform.function.pluginId
import taboolib.common.platform.function.warning
import taboolib.library.reflex.ClassMethod
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.kether.Kether
import taboolib.module.kether.ScriptActionParser
import taboolib.module.kether.StandardChannel
import top.lanscarlos.vulpecula.bacikal.parser.BacikalParser
import top.lanscarlos.vulpecula.bacikal.property.BacikalGenericProperty
import top.lanscarlos.vulpecula.bacikal.property.BacikalProperty
import java.util.function.Supplier

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal
 *
 * @author Lanscarlos
 * @since 2023-08-27 22:55
 */
@Awake(LifeCycle.LOAD)
object BacikalRegistry : ClassVisitor(-1) {

    @Config("action-registry.yml")
    lateinit var actionRegistry: Configuration
        private set

    @Config("property-registry.yml")
    lateinit var propertyRegistry: Configuration
        private set

    override fun getLifeCycle() = LifeCycle.LOAD

    /**
     * 注册语句
     * */
    override fun visit(method: ClassMethod, clazz: Class<*>, instance: Supplier<*>?) {
        if (!method.isAnnotationPresent(BacikalParser::class.java) || method.returnType != ScriptActionParser::class.java) {
            return
        }

        // 加载注解
        val annotation = method.getAnnotation(BacikalParser::class.java)
        val id = annotation.property<String>("id") ?: return

        if (actionRegistry[id] == null) {
            // 配置文件中不存在该语句
            warning("Action \"$id\" was not register in action-registry.yml")
            return
        }

        if (actionRegistry.getBoolean("$id.disable", false)) {
            // 该语句已被禁用
            return
        }

        // 获取语句对象
        val parser = if (instance != null) {
            method.invoke(instance.get()) as ScriptActionParser<*>
        } else {
            method.invokeStatic() as ScriptActionParser<*>
        }

        // 获取本地注册信息
        val local = actionRegistry.getStringList("$id.local").mapNotNull {
            val cache = it.split(":")
            if (cache.size != 2) {
                warning("Action \"$id\" local message \"$it\" is not valid.")
                return@mapNotNull null
            }
            cache[0] to cache[1]
        }

        // 本地注册
        for ((namespace, name) in local) {
            Kether.scriptRegistry.registerAction(namespace, name, parser)
        }

        if (!actionRegistry.getBoolean("$id.share", true)) {
            // 私有语句
            return
        }

        // 获取远程注册信息
        val remote = actionRegistry.getStringList("$id.remote").mapNotNull {
            val cache = it.split(":")
            if (cache.size != 2) {
                warning("Action \"$id\" remote message \"$it\" is not valid.")
                return@mapNotNull null
            }
            cache[0] to cache[1]
        }.ifEmpty { local }.groupBy(
            { it.first },
            { it.second }
        )

        // 远程注册
        for (connection in getOpenContainers()) {
            if (connection.name == pluginId) {
                continue
            }

            for ((namespace, name) in remote) {
                connection.call(
                    StandardChannel.REMOTE_ADD_ACTION,
                    arrayOf(namespace, name, parser)
                )
            }
        }
    }

    /**
     * 注册属性
     * */
    override fun visitStart(clazz: Class<*>, instance: Supplier<*>?) {
        if (!clazz.isAnnotationPresent(BacikalProperty::class.java)) {
            return
        }

        if (!BacikalGenericProperty::class.java.isAssignableFrom(clazz)) {
            return
        }

        // 加载注解
        val annotation = clazz.getAnnotation(BacikalProperty::class.java)

        if (propertyRegistry.getBoolean("${annotation.id}.disable", false)) {
            // 该属性已被禁用
            return
        }

        // 获取属性对象
        val property = if (instance != null) {
            instance.get() as BacikalGenericProperty<*>
        } else {
            try {
                clazz.getDeclaredConstructor().newInstance() as BacikalGenericProperty<*>
            } catch (ex: Exception) {
                ex.printStackTrace()
                return
            }
        }

        // 本地注册
        Kether.registeredScriptProperty.computeIfAbsent(annotation.bind.java) { HashMap() }[property.id] = property

        if (!propertyRegistry.getBoolean("${annotation.id}.share", true)) {
            // 私有属性
            return
        }

        val remoteName = annotation.bind.java.name.let {
            if (it.startsWith(taboolibPath)) "@${it.substring(taboolibPath.length)}" else it
        }

        // 远程注册
        for (connection in getOpenContainers()) {
            if (connection.name == pluginId) {
                continue
            }

            connection.call(
                StandardChannel.REMOTE_ADD_PROPERTY,
                arrayOf(pluginId, remoteName, property)
            )
        }
    }
}
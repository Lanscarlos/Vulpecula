package top.lanscarlos.vulpecula.bacikal

import taboolib.common.io.taboolibPath
import taboolib.common.platform.function.getOpenContainers
import taboolib.common.platform.function.pluginId
import taboolib.common.platform.function.warning
import taboolib.library.kether.QuestActionParser
import taboolib.library.reflex.ClassMethod
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.kether.Kether
import taboolib.module.kether.ScriptActionParser
import taboolib.module.kether.StandardChannel
import top.lanscarlos.vulpecula.internal.ClassInjector
import java.util.function.Supplier

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal
 *
 * @author Lanscarlos
 * @since 2023-03-19 22:08
 */
object BacikalRegistry : ClassInjector() {

    @Config("action-registry.yml")
    lateinit var actionRegistry: Configuration
        private set

    @Config("property-registry.yml")
    lateinit var propertyRegistry: Configuration
        private set

    /**
     * 访问函数
     * */
    override fun visit(method: ClassMethod, clazz: Class<*>, supplier: Supplier<*>?) {
        registerAction(method, supplier)
    }

    /**
     * 访问类
     * */
    override fun visitStart(clazz: Class<*>, supplier: Supplier<*>?) {
        registerProperty(clazz, supplier)
    }

    /**
     * 函数式语句注册
     * */
    private fun registerAction(method: ClassMethod, instance: Supplier<*>?) {
        if (!method.isAnnotationPresent(BacikalParser::class.java) || method.returnType != ScriptActionParser::class.java) {
            return
        }

        // 加载注解
        val annotation = method.getAnnotation(BacikalParser::class.java)
        val id = annotation.property<String>("id") ?: return

        // 获取语句对象
        val parser = if (instance != null) {
            method.invoke(instance.get()) as ScriptActionParser<*>
        } else {
            method.invokeStatic() as ScriptActionParser<*>
        }

        // 注册语句
        registerAction(id, parser)
    }

    /**
     * 注册语句
     * */
    fun registerAction(id: String, parser: QuestActionParser) {

        if (actionRegistry[id] == null) {
            // 配置文件中不存在该语句
            warning("Action \"$id\" was not register in action-registry.yml")
            return
        }

        if (actionRegistry.getBoolean("$id.disable", false)) {
            // 该语句已被禁用
            return
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

        // 获取远程注册信息
        val remote = actionRegistry.getStringList("$id.remote").mapNotNull {
            val cache = it.split(":")
            if (cache.size != 2) {
                warning("Action \"$id\" remote message \"$it\" is not valid.")
                return@mapNotNull null
            }
            cache[0] to cache[1]
        }.groupBy(
            { it.first },
            { it.second }
        )

        if (remote.isEmpty()) {
            // 无远程注册信息, 即私有语句
            return
        }

        // 远程注册
        for (connection in getOpenContainers()) {
            if (connection.name == pluginId) {
                continue
            }

            for ((namespace, name) in remote) {
                connection.call(
                    StandardChannel.REMOTE_ADD_ACTION,
                    arrayOf(pluginId, name, namespace)
                )
            }
        }
    }

    /**
     * 判断是否存在语句
     * */
    fun hasAction(id: String): Boolean {
        if (actionRegistry[id] == null) {
            // 配置文件中不存在该语句
            return false
        }
        return !actionRegistry.getBoolean("$id.disable", false)
    }

    /**
     * 类式属性注册
     * */
    private fun registerProperty(clazz: Class<*>, instance: Supplier<*>?) {
        if (!clazz.isAnnotationPresent(BacikalProperty::class.java)) {
            return
        }

        if (!BacikalGenericProperty::class.java.isAssignableFrom(clazz)) {
            return
        }

        // 加载注解
        val annotation = clazz.getAnnotation(BacikalProperty::class.java)

        // 获取属性对象
        val property = if (instance != null) {
            instance.get() as BacikalGenericProperty<*>
        } else try {
            // 尝试实例化
            clazz.getDeclaredConstructor().newInstance() as BacikalGenericProperty<*>
        } catch (ex: Exception) {
            warning("Property \"${clazz.name}\" must have a empty constructor.")
            return
        }

        // 注册属性
        registerProperty(annotation.id, annotation.bind.java, property)
    }

    /**
     * 注册属性
     * */
    fun registerProperty(id: String, bind: Class<*>, property: BacikalGenericProperty<*>) {

        if (propertyRegistry.getBoolean("$id.disable", false)) {
            // 该属性已被禁用
            return
        }

        // 本地注册
        Kether.registeredScriptProperty.computeIfAbsent(bind) { HashMap() }[property.id] = property

        if (!propertyRegistry.getBoolean("$id.share", true)) {
            // 私有属性
            return
        }

        val remoteName = bind.name.let {
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
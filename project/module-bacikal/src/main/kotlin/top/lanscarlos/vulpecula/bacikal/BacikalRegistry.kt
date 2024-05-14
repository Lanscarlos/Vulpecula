package top.lanscarlos.vulpecula.bacikal

import taboolib.common.ClassAppender
import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitor
import taboolib.common.io.getClasses
import taboolib.common.io.getInstance
import taboolib.common.io.taboolibPath
import taboolib.common.platform.Awake
import taboolib.common.platform.function.*
import taboolib.library.kether.QuestAction
import taboolib.library.kether.QuestActionParser
import taboolib.library.kether.QuestReader
import taboolib.library.reflex.ClassMethod
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.kether.Kether
import taboolib.module.kether.ScriptActionParser
import taboolib.module.kether.StandardChannel
import top.lanscarlos.vulpecula.bacikal.parser.*
import top.lanscarlos.vulpecula.bacikal.property.BacikalGenericProperty
import top.lanscarlos.vulpecula.bacikal.property.BacikalProperty
import java.io.File
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
     * 访问函数
     * */
    override fun visit(method: ClassMethod, clazz: Class<*>, instance: Supplier<*>?) {
        registerAction(method, instance)
    }

    /**
     * 访问类
     * */
    override fun visitStart(clazz: Class<*>, instance: Supplier<*>?) {
//        registerAction(clazz)
        registerProperty(clazz, instance)
    }

    /**
     * 外置语句注册
     *
     * @param file 外置语句 Jar 包体
     * */
    fun registerAction(file: File) {
        if (!file.exists() || !file.isFile || !file.canRead()) {
            warning("Action file \"${file.name}\" is not valid.")
            return
        }

        ClassAppender.addPath(file.toPath(), false, false)
        val classes = file.toURI().toURL().getClasses().values

        val headers = mutableMapOf<String, BacikalComplexActionParser>()
        val bodies = mutableMapOf<String, ArrayList<Pair<String, BacikalActionParser>>>()

        // 遍历所有 class 对象
        for (clazz in classes) {
            if (clazz.name.contains("taboolib")) {
                // 排除 taboolib 库
                continue
            }
            when {
                clazz.isAnnotationPresent(BacikalParserBody::class.java) -> {
                    if (!BacikalActionParser::class.java.isAssignableFrom(clazz)) {
                        warning("Action body class \"${clazz.name}\" must be a subclass of \"BacikalActionParser\".")
                        continue
                    }
                    val annotation = clazz.getAnnotation(BacikalParserBody::class.java)
                    val parser = clazz.getInstance(true)?.get() as? BacikalActionParser
                        ?: error("Action class \"${clazz.name}\" must have a empty constructor.")
                    bodies.computeIfAbsent(annotation.bind) { ArrayList() } += annotation.id to parser
                }
                clazz.isAnnotationPresent(BacikalParser::class.java) -> {
                    // 加载解析器
                    val annotation = clazz.getAnnotation(BacikalParser::class.java)
                    val id = annotation.id

                    if (BacikalComplexActionParser::class.java.isAssignableFrom(clazz)) {
                        val parser = clazz.getInstance(true)?.get() as? BacikalComplexActionParser
                            ?: error("Action class \"${clazz.name}\" must have a empty constructor.")
                        headers[id] = parser
                    } else if (BacikalActionParser::class.java.isAssignableFrom(clazz)) {
                        val parser = clazz.getInstance(true)?.get() as? BacikalActionParser
                            ?: error("Action class \"${clazz.name}\" must have a empty constructor.")
                        registerAction(id, parser)
                    } else {
                        error("Action class \"${clazz.name}\" must be a subclass of \"BacikalActionParser\" or \"BacikalComplexActionParser\".")
                    }
                }
            }
        }

        // 遍历所有复合解析器
        for ((id, header) in headers) {
            for ((name, body) in bodies[id] ?: continue) {
                header.registerAction(name, body)
            }
            registerAction(id, header)
        }
    }

    /**
     * 函数式语句注册
     * */
    fun registerAction(method: ClassMethod, instance: Supplier<*>?) {
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
     * 类式语句注册
     * */
    @Suppress("UNCHECKED_CAST")
    @Deprecated("Deprecated")
    fun registerAction(clazz: Class<*>) {
        if (!clazz.isAnnotationPresent(BacikalParser::class.java)) {
            return
        }

        // 限定类型
        if (!QuestAction::class.java.isAssignableFrom(clazz)) {
            return
        }

        // 加载注解
        val annotation = clazz.getAnnotation(BacikalParser::class.java)
        val id = annotation.id

        // 获取构造器
        val constructor = try {
            clazz.getDeclaredConstructor(BacikalContext::class.java)
        } catch (ex: NoSuchMethodException) {
            warning("Action \"${clazz.name}\" must have a constructor with a parameter of type \"BacikalContext\"")
            return
        }

        // 获取解析器
        val parser = object : QuestActionParser {
            override fun <T> resolve(reader: QuestReader): QuestAction<T>? {
                return constructor.newInstance(DefaultContext(reader)) as? QuestAction<T>
            }
        }

        // 注册语句
        registerAction(id, parser)
    }

    /**
     * 注册语句
     * */
    fun registerAction(id: String, parser: QuestActionParser) {

        info("Registering action \"$id\"")

        if (actionRegistry[id] == null) {
            // 配置文件中不存在该语句
            warning("Action \"$id\" was not register in action-registry.yml")
            return
        }

        if (actionRegistry.getBoolean("$id.disable", false)) {
            // 该语句已被禁用
            info("Action \"$id\" is disabled.")
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
                    arrayOf(pluginId, name, namespace)
                )
            }
        }
    }

    /**
     * 函数式属性注册
     * */
    fun registerProperty() {}

    /**
     * 类式属性注册
     * */
    fun registerProperty(clazz: Class<*>, instance: Supplier<*>?) {
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
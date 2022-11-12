package top.lanscarlos.vulpecula.kether

import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitor
import taboolib.common.io.taboolibPath
import taboolib.common.platform.Awake
import taboolib.common.platform.function.*
import taboolib.common.util.asList
import taboolib.library.reflex.ClassMethod
import taboolib.module.kether.Kether
import taboolib.module.kether.ScriptActionParser
import taboolib.module.kether.ScriptProperty
import taboolib.module.kether.StandardChannel
import taboolib.module.lang.sendLang
import top.lanscarlos.vulpecula.utils.timing
import top.lanscarlos.vulpecula.utils.toConfig
import java.io.File
import java.util.function.Supplier

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether
 *
 * @author Lanscarlos
 * @since 2022-10-18 16:33
 */
@Awake(LifeCycle.LOAD)
class KetherRegistry : ClassVisitor(1) {

    override fun getLifeCycle() = LifeCycle.LOAD

    // 加载 Vulpecula 脚本属性
    override fun visitStart(clazz: Class<*>, supplier: Supplier<*>?) {
        if (!clazz.isAnnotationPresent(VulKetherProperty::class.java) || !VulScriptProperty::class.java.isAssignableFrom(clazz)) return

        // 加载属性对象
        val property = let {
            if (supplier?.get() != null) {
                supplier.get()
            } else try {
                clazz.getDeclaredConstructor().newInstance()
            } catch (e: Exception) {
                null
            }
        } as? VulScriptProperty<*> ?: return

        // 加载注解
        val annotation = clazz.getAnnotation(VulKetherProperty::class.java)

        // 是否禁用属性
        if (config.getBoolean("property.${annotation.id}.disable", false)) return

        // 是否分享泛型属性
        if (annotation.generic && annotation.shared) {
            if (config.getBoolean("action.${annotation.id}.shared", true)) {
                var name = annotation.bind.java.name
                name = if (name.startsWith(taboolibPath)) "@${name.substring(taboolibPath.length)}" else name
                getOpenContainers().forEach {
                    it.call(StandardChannel.REMOTE_ADD_PROPERTY, arrayOf(pluginId, name, property))
                }
            }
        }

        if (annotation.generic) {
            // 仅分享泛型属性
            Kether.registeredScriptProperty.computeIfAbsent(annotation.bind.java) { HashMap() }[property.id] = property
        } else {
            registerScriptProperty(annotation.bind.java, property)
        }
    }

    // 加载 Vulpecula 语句
    override fun visit(method: ClassMethod, clazz: Class<*>, instance: Supplier<*>?) {
        if (!method.isAnnotationPresent(VulKetherParser::class.java) || method.returnType != ScriptActionParser::class.java) return

        // 加载注解
        val annotation = method.getAnnotation(VulKetherParser::class.java)

        val id = annotation.property<String>("id") ?: return

        // 是否禁用语句
        if (config.getBoolean("action.$id.disable", false)) return

        // 加载注解属性
        val name = annotation.property<Any>("name")?.asList()?.toTypedArray() ?: arrayOf()
        val namespace = annotation.property("namespace", "vulpecula")
        val override = annotation.property<Any>("override")?.asList()?.toTypedArray() ?: arrayOf()
        val injectDefaultNamespace = config.getBoolean("action.$id.inject-default-namespace", true)
        val overrideDefaultAction = config.getBoolean("action.$id.override-default-action", false)

        // 是否分享语句
        val shared = if (annotation.property("shared", true)) {
            config.getBoolean("action.$id.shared", true)
        } else {
            false
        }

        // 获取解析器
        val parser = (if (instance == null) method.invokeStatic() else method.invoke(instance.get())) as ScriptActionParser<*>
        parserRegistry[id] = ParserMetadata(id, parser, name, namespace, shared, override, injectDefaultNamespace, overrideDefaultAction)

        // 注册语句
        name.forEach {
            Kether.scriptRegistry.registerAction("vul", it, parser)
            if (namespace != "kether") {
                // 防止注入原生命名空间
                Kether.scriptRegistry.registerAction(namespace, it, parser)
            }
        }
    }

    companion object {

        private val config by lazy {
            val file = File(getDataFolder(), "kether-registry.yml")
            if (!file.exists()) {
                releaseResourceFile("kether-registry.yml", true)
            }
            file.toConfig()
        }

        private val parserRegistry = HashMap<String, ParserMetadata>()
        private val propertyRegistry = HashMap<Class<*>, ScriptProperty<*>>()
        private var propertyCache: Collection<Pair<Class<*>, ScriptProperty<*>>> = emptyList()

        @Suppress("UNCHECKED_CAST")
        fun <T> getScriptProperties(instance: T): Collection<ScriptProperty<in T>> {
            return propertyCache.filter {
                it.first.isInstance(instance)
            }.sortedWith { c1, c2 ->
                if (c1.first.isAssignableFrom(c2.first)) 1 else -1
            }.mapNotNull {
                it.second as? ScriptProperty<in T>
            }
        }

        fun registerScriptProperty(key: Class<*>, property: ScriptProperty<*>) {
            propertyRegistry[key] = property
            // 提前解析为列表方便后续过滤
            propertyCache = propertyRegistry.map { it.key to it.value }
        }

        fun unregisterScriptProperty(key: Class<*>) {
            propertyRegistry.remove(key)
            // 提前解析为列表方便后续过滤
            propertyCache = propertyRegistry.map { it.key to it.value }
        }

        /**
         * 使用 ENABLE 防止被原生加载器覆盖语句解析器
         * */
        @Awake(LifeCycle.ENABLE)
        fun registerAction() {
            val start = timing()
            try {
                parserRegistry.forEach { (id, meta) ->

                    // 注入原生命名空间
                    if (meta.injectDefaultNamespace) {
                        meta.name.forEach {
                            Kether.scriptRegistry.registerAction("kether", it, meta.parser)
                        }
                    }

                    // 覆盖原生语句
                    if (meta.overrideDefaultAction && meta.override.isNotEmpty()) {
                        meta.override.forEach {
                            Kether.scriptRegistry.registerAction("kether", it, meta.parser)
                        }
                        console().sendLang("Kether-Override-Action-Local-Succeeded", id, timing(start))
                    }

                    // 是否分享语句
                    if (!meta.shared) return@forEach

                    getOpenContainers().forEach inner@{

                        if (it.name == pluginId) return@inner

                        it.call(StandardChannel.REMOTE_ADD_ACTION, arrayOf(pluginId, meta.name, "vul"))
                        if (meta.namespace != "kether") {
                            // 防止注入原生命名空间
                            it.call(StandardChannel.REMOTE_ADD_ACTION, arrayOf(pluginId, meta.name, meta.namespace))
                        }

                        // 注入原生命名空间
                        if (meta.injectDefaultNamespace) {
                            it.call(StandardChannel.REMOTE_ADD_ACTION, arrayOf(pluginId, meta.name, "kether"))
                        }

                        // 覆盖原生语句
                        if (meta.overrideDefaultAction && meta.override.isNotEmpty()) {
                            it.call(StandardChannel.REMOTE_ADD_ACTION, arrayOf(pluginId, meta.override, "kether"))
                        }
                    }

                    if (meta.overrideDefaultAction && meta.override.isNotEmpty()) {
                        console().sendLang("Kether-Override-Action-Remote-Succeeded", id, timing(start))
                    }
                }
            } catch (e: Exception) {
                console().sendLang("Kether-Action-Register-Failed", e.localizedMessage, timing(start))
            }

        }

    }

}
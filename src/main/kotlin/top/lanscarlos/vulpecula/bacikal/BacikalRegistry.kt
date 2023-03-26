package top.lanscarlos.vulpecula.bacikal

import taboolib.common.LifeCycle
import taboolib.common.io.taboolibPath
import taboolib.common.platform.Awake
import taboolib.common.platform.function.*
import taboolib.common.util.asList
import taboolib.library.reflex.ClassMethod
import taboolib.module.kether.Kether
import taboolib.module.kether.ScriptActionParser
import taboolib.module.kether.StandardChannel
import taboolib.module.lang.sendLang
import top.lanscarlos.vulpecula.internal.ClassInjector
import top.lanscarlos.vulpecula.utils.timing
import top.lanscarlos.vulpecula.utils.toConfig
import java.io.File
import java.util.function.Supplier

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal
 *
 * @author Lanscarlos
 * @since 2023-03-19 22:08
 */
object BacikalRegistry : ClassInjector() {

    private val parserRegistry = HashMap<String, RegistryMetadata>()

    // 配置文件，仅加载一次
    private val actionConfig by lazy {
        val file = File(getDataFolder(), "actions/action-registry.yml")
        if (!file.exists()) {
            releaseResourceFile("actions/action-registry.yml", true)
        }
        file.toConfig()
    }

    private val propertyConfig by lazy {
        val file = File(getDataFolder(), "actions/property-registry.yml")
        if (!file.exists()) {
            releaseResourceFile("actions/property-registry.yml", true)
        }
        file.toConfig()
    }

    /**
     * 判断语句是否被注册
     * */
    fun hasAction(vararg name: String): Boolean {
        if (name.isEmpty()) return false
        else if (name.size == 1) return name[0] in parserRegistry

        for (it in name) {
            if (it in parserRegistry) return true
        }
        return false
    }

    // 加载 Vulpecula 脚本属性
    override fun visitStart(clazz: Class<*>, supplier: Supplier<*>?) {
        if (!clazz.isAnnotationPresent(BacikalProperty::class.java) || !BacikalScriptProperty::class.java.isAssignableFrom(clazz)) return

        // 加载注解
        val annotation = clazz.getAnnotation(BacikalProperty::class.java)

        // 是否禁用属性
        if (propertyConfig.getBoolean("${annotation.id}.disable", false)) return

        // 加载属性对象
        val property = let {
            if (supplier?.get() != null) {
                supplier.get()
            } else try {
                clazz.getDeclaredConstructor().newInstance()
            } catch (e: Exception) {
                null
            }
        } as? BacikalScriptProperty<*> ?: return

        if (annotation.shared) {
            // 是否分享属性
            if (propertyConfig.getBoolean("${annotation.id}.shared", true)) {
                var name = annotation.bind.java.name
                name = if (name.startsWith(taboolibPath)) "@${name.substring(taboolibPath.length)}" else name
                getOpenContainers().forEach {
                    it.call(StandardChannel.REMOTE_ADD_PROPERTY, arrayOf(pluginId, name, property))
                }
            }
        }

        // 向 Kether 注入属性
        Kether.registeredScriptProperty.computeIfAbsent(annotation.bind.java) { HashMap() }[property.id] = property
    }

    // 加载 Vulpecula 语句
    override fun visit(method: ClassMethod, clazz: Class<*>, supplier: Supplier<*>?) {
        if (!method.isAnnotationPresent(BacikalParser::class.java) || method.returnType != ScriptActionParser::class.java) return

        // 加载注解
        val annotation = method.getAnnotation(BacikalParser::class.java)

        val id = annotation.property<String>("id") ?: return

        // 是否禁用语句
        if (actionConfig.getBoolean("$id.disable", false)) return

        // 加载注解属性
        val name = annotation.property<Any>("name")?.asList()?.toTypedArray() ?: arrayOf()
        val namespace = annotation.property("namespace", "vulpecula")
        val override = annotation.property<Any>("override")?.asList()?.toTypedArray() ?: arrayOf()
        val injectDefaultNamespace = actionConfig.getBoolean("$id.inject-default-namespace", true)
        val overrideDefaultAction = actionConfig.getBoolean("$id.override-default-action", false)

        // 是否分享语句
        val shared = if (annotation.property("shared", true)) {
            actionConfig.getBoolean("$id.shared", true)
        } else {
            false
        }

        // 获取解析器
        val parser = (if (supplier == null) method.invokeStatic() else method.invoke(supplier.get())) as ScriptActionParser<*>
        parserRegistry[id] = RegistryMetadata(id, parser, name, namespace, shared, override, injectDefaultNamespace, overrideDefaultAction)

        // 注册语句
        name.forEach {
            Kether.scriptRegistry.registerAction("vul", it, parser)
            if (namespace != "kether") {
                // 防止注入原生命名空间
                Kether.scriptRegistry.registerAction(namespace, it, parser)
            }
        }
    }

    /**
     * 使用 ENABLE 防止被原生加载器覆盖语句解析器
     * */
    @Awake(LifeCycle.ENABLE)
    fun autoRegisterAction() {
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
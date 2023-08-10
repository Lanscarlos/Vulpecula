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
        if (!clazz.isAnnotationPresent(BacikalProperty::class.java) || !BacikalGenericProperty::class.java.isAssignableFrom(
                clazz
            )
        ) return

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
        } as? BacikalGenericProperty<*> ?: return

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

        if (actionConfig[id] == null) {
            // 未在配置文件中检测到语句
            warning("Action \"$id\" was not found in action-registry.yml!")
        }

        // 是否禁用语句
        if (actionConfig.getBoolean("$id.disable", false)) return

        // 加载注解属性
        val aliases = annotation.property<Any>("aliases")?.asList()?.toTypedArray() ?: arrayOf()
        val namespace = arrayOf("kether", "vul", annotation.property("namespace", "vulpecula"))
        val injectDefaultNamespace = actionConfig.getBoolean("$id.inject-default-namespace", false)

        // 是否分享语句
        val shared = if (annotation.property("shared", true)) {
            actionConfig.getBoolean("$id.shared", true)
        } else {
            false
        }

        // 获取解析器
        val parser =
            (if (supplier == null) method.invokeStatic() else method.invoke(supplier.get())) as ScriptActionParser<*>
        parserRegistry[id] = RegistryMetadata(id, parser, aliases, namespace, shared, injectDefaultNamespace)

        // 注册语句
        for (name in aliases) {
            for (space in namespace) {
                if (space == "vulpecula" || space == "vul" || name.last() == '*' || name.startsWith("vul-")) {
                    // 本地命名空间 或 Legacy 拓展标识，直接注册
                    Kether.scriptRegistry.registerAction(space, name, parser)
                    continue
                }

                Kether.scriptRegistry.registerAction(space, "v-$name", parser)
            }
        }
    }

    /**
     * 使用 ENABLE 防止被原生加载器覆盖语句解析器
     * */
    @Awake(LifeCycle.ENABLE)
    fun registerAction() {
        val start = timing()
        try {
            for ((_, meta) in parserRegistry) {

                if (meta.injectDefaultNamespace) {
                    // 允许注入默认命名空间
                    for (name in meta.aliases) {
                        if (name.last() == '*') {
                            // 忽略已注册过的 Legacy 拓展标识
                            continue
                        }
                        Kether.scriptRegistry.registerAction("kether", name, meta.parser)
                    }
                }

                if (!meta.shared) {
                    // 私有语句
                    continue
                }

                for (container in getOpenContainers()) {
                    if (container.name == pluginId) {
                        // 不分享给自己
                        continue
                    }

                    for (space in meta.namespace) {
                        if (space == "kether" && meta.injectDefaultNamespace) {
                            // 允许注入默认命名空间
                            container.call(
                                StandardChannel.REMOTE_ADD_ACTION,
                                arrayOf(pluginId, meta.aliases.filter { it.last() != '*' }, space)
                            )
                        }

                        val aliases =
                            meta.aliases.map { if (it.last() == '*' || it.startsWith("vul-")) it else "v-$it" }
                        container.call(StandardChannel.REMOTE_ADD_ACTION, arrayOf(pluginId, aliases, space))
                    }
                }
            }
            console().sendLang("Kether-Override-Action-Remote-Succeeded", timing(start))
        } catch (e: Exception) {
            console().sendLang("Kether-Action-Register-Failed", e.localizedMessage, timing(start))
        }

    }
}
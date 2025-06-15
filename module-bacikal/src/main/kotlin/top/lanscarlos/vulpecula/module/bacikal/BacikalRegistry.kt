package top.lanscarlos.vulpecula.module.bacikal

import taboolib.common.ClassAppender
import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitor
import taboolib.common.io.getClasses
import taboolib.common.io.getResources
import taboolib.common.platform.Awake
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.getOpenContainers
import taboolib.common.platform.function.info
import taboolib.common.platform.function.pluginId
import taboolib.common.platform.function.releaseResourceFolder
import taboolib.common.platform.function.warning
import taboolib.library.kether.QuestActionParser
import taboolib.library.reflex.ReflexClass
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.kether.Kether
import taboolib.module.kether.StandardChannel
import top.lanscarlos.vulpecula.module.bacikal.annotation.BacikalParser
import top.lanscarlos.vulpecula.module.bacikal.parser.BacikalActionParser
import top.lanscarlos.vulpecula.module.bacikal.parser.BacikalActionResolver
import top.lanscarlos.vulpecula.module.bacikal.parser.BacikalComplexActionParser
import java.io.File
import java.util.Base64

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal
 *
 * 注册中心
 *
 * @author Lanscarlos
 * @since 2024-11-20 16:33
 */
@Awake(LifeCycle.LOAD)
object BacikalRegistry : ClassVisitor(1) {

    @Config("bacikal-registry.yml")
    lateinit var registry: Configuration
        private set

    val headers = mutableMapOf<String, BacikalComplexActionParser>()

    val metadata = mutableMapOf<String, List<String>>()

    @Awake(LifeCycle.LOAD)
    fun onLoad() {
        // 注册拓展语句
        val folder = File(getDataFolder(), "action")
        if (!folder.exists()) {
            releaseResourceFolder("action")
        }

        for (file in folder.listFiles()) {
            if (!file.exists() || !file.isFile || !file.canRead()) {
                continue
            }
            if (file.extension != "jar") {
                continue
            }
            registerAction(file)
        }
    }

    @Awake(LifeCycle.ENABLE)
    fun onEnable() {
        for ((id, parser) in headers) {
            registerAction(id, parser)
        }
    }

    override fun visitStart(owner: ReflexClass) {
        if (!owner.hasAnnotation(BacikalParser::class.java)) {
            return
        }
        info("Registering action ${owner.name}")
        for (annotation in owner.structure.annotations) {
            info("    - Reflex Annotation: ${annotation.source.name}")
        }
        for (annotation in owner.toClass().annotations) {
            info("    - Java Annotation: ${annotation.annotationClass.qualifiedName}")
        }
        registerAction(owner)
    }

    /**
     * 外置语句注册
     *
     * @param file 外置语句 Jar 包体
     * */
    fun registerAction(file: File) {
        if (!file.exists() || !file.isFile || !file.canRead() || file.extension != "jar") {
            error("Action file \"${file.name}\" is not valid.")
        }

        ClassAppender.addPath(file.toPath(), false, false)
        val owners = file.toURI().toURL().getClasses().values

        // 遍历资源
        for ((name, byteArray) in file.toURI().toURL().getResources()) {
            if (!name.endsWith(".metadata")) {
                continue
            }
            val array = byteArray.toString()
                .split("\\R".toRegex())
                .map { Base64.getDecoder().decode(it).toString(Charsets.ISO_8859_1) }
//                .toTypedArray() TODO
            metadata[name.substringBeforeLast('.')] = array
        }

        // 遍历所有 class 对象
        for (owner in owners) {
            if (!owner.hasAnnotation(BacikalParser::class.java)) {
                // 排除非注解类的注册
                continue
            }
            registerAction(owner)
        }
    }

    /**
     * 类式语句注册
     */
    fun registerAction(owner: ReflexClass) {
        if (!owner.hasInterface(BacikalActionResolver::class.java)) {
            error("BacikalRegistry#registerAction >> Cannot register class ${owner.name} without BacikalActionResolver interface.")
        }
        val resolver = (findInstance(owner) ?: owner.newInstance()) as? BacikalActionResolver
            ?: error("BacikalRegistry#registerAction >> Cannot create instance of ${owner.name}")

        val parser = BacikalActionParser(owner.toClass(), resolver)

        if (resolver.bind != null) {
            // 绑定主体
            val header = headers.computeIfAbsent(resolver.bind!!) { BacikalComplexActionParser(it) }
            header.registerAction(resolver.id, parser)
        } else {
            registerAction(resolver.id, parser)
        }
    }

    /**
     * 注册语句
     *
     * @param id 语句ID
     * @param parser 语句解析器
     */
    fun registerAction(id: String, parser: QuestActionParser) {
        // 读取本地注册信息
        val local = registry.getStringList("actions.$id.local").mapNotNull {
            val cache = it.split(":")
            if (cache.size != 2) {
                warning("Action \"$id\" local message \"$it\" is not valid.")
                return@mapNotNull null
            }
            cache[0] to cache[1]
        }

        // 读取远程注册信息
        val remote = registry.getStringList("actions.$id.remote").mapNotNull {
            val cache = it.split(":")
            if (cache.size != 2) {
                warning("Action \"$id\" remote message \"$it\" is not valid.")
                return@mapNotNull null
            }
            cache[0] to cache[1]
        }

        registerAction(id, parser, local, remote)
    }

    /**
     * 注册语句
     *
     * @param local 本地注册信息 namespace to name
     * @param remote 远程注册信息 namespace to name
     */
    fun registerAction(id: String, parser: QuestActionParser, local: List<Pair<String, String>>, remote: List<Pair<String, String>>) {
        info("Registering action \"$id\":")
        for ((namespace, name) in local) {
            Kether.scriptRegistry.registerAction(namespace, name, parser)
            info("    - Local($pluginId) $namespace:$name")
        }

        val map = remote.groupBy({ it.first }, { it.second })
        for (connection in getOpenContainers()) {
            if (connection.name == pluginId) {
                // 过滤自身插件
                continue
            }

            for ((namespace, name) in map) {
                connection.call(
                    StandardChannel.REMOTE_ADD_ACTION,
                    arrayOf(pluginId, name, namespace)
                )
                info("    - Remote(${connection.name}) $namespace:$name")
            }
        }
    }

    override fun getLifeCycle(): LifeCycle {
        return LifeCycle.LOAD
    }

}
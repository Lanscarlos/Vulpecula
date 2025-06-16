package top.lanscarlos.vulpecula.module.bacikal.action

import taboolib.common.ClassAppender
import taboolib.common.inject.ClassVisitor.findInstance
import taboolib.common.io.getClasses
import taboolib.common.io.getResources
import taboolib.common.platform.function.getOpenContainers
import taboolib.common.platform.function.info
import taboolib.common.platform.function.pluginId
import taboolib.common.platform.function.warning
import taboolib.library.kether.QuestActionParser
import taboolib.library.reflex.ReflexClass
import taboolib.module.kether.Kether
import taboolib.module.kether.StandardChannel
import top.lanscarlos.vulpecula.module.bacikal.BacikalRegistry.headers
import top.lanscarlos.vulpecula.module.bacikal.BacikalRegistry.metadata
import top.lanscarlos.vulpecula.module.bacikal.BacikalRegistry.registry
import top.lanscarlos.vulpecula.module.bacikal.annotation.BacikalParser
import top.lanscarlos.vulpecula.module.bacikal.parser.ReflexActionParser
import top.lanscarlos.vulpecula.module.bacikal.parser.ReflexActionResolver
import top.lanscarlos.vulpecula.module.bacikal.parser.ComplexActionParser
import java.io.File
import java.util.Base64
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.ifEmpty
import kotlin.collections.iterator

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.action
 *
 * @author Lanscarlos
 * @since 2025/6/16
 */
object ActionClassRegister {

    /**
     * 类式语句注册
     */
    fun registerAction(owner: ReflexClass) {
        if (!owner.hasInterface(ReflexActionResolver::class.java)) {
            error("BacikalRegistry#registerAction >> Cannot register class ${owner.name} without BacikalActionResolver interface.")
        }
        val resolver = (findInstance(owner) ?: owner.newInstance()) as? ReflexActionResolver
            ?: error("BacikalRegistry#registerAction >> Cannot create instance of ${owner.name}")

        val annotation = owner.toClass().getAnnotation(BacikalParser::class.java)
        val parser = ReflexActionParser(
            annotation.id,
            annotation.aliases,
            annotation.bind,
            annotation.namespace,
            annotation.description,
            owner.toClass(),
            resolver
        )

        if (resolver.bind != null) {
            // 绑定主体
            val header = headers.computeIfAbsent(resolver.bind!!) { ComplexActionParser(it) }
            header.registerAction(resolver.id, parser)
        } else {
            registerAction(resolver.id, parser)
        }
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

        // 遍历资源
        for ((name, byteArray) in file.toURI().toURL().getResources()) {
            if (!name.endsWith(".metadata")) {
                continue
            }
            val key = name.substringAfterLast("/").substringBeforeLast('.')
            val array = String(byteArray)
                .split("\\R".toRegex())
                .map { Base64.getDecoder().decode(it).toString(Charsets.ISO_8859_1) }
                .toTypedArray()
            metadata[key] = array
        }

        // 遍历所有 class 对象
        for (owner in file.toURI().toURL().getClasses().values) {
            if (!owner.hasAnnotation(BacikalParser::class.java)) {
                // 排除非注解类的注册
                continue
            }
            registerAction(owner)
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
        }.ifEmpty {
            listOf("vulpecula" to id)
        }

        // 读取远程注册信息
        val remote = registry.getStringList("actions.$id.remote").mapNotNull {
            val cache = it.split(":")
            if (cache.size != 2) {
                warning("Action \"$id\" remote message \"$it\" is not valid.")
                return@mapNotNull null
            }
            cache[0] to cache[1]
        }.ifEmpty {
            listOf("vulpecula" to id)
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

}
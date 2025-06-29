package top.lanscarlos.vulpecula.module.bacikal

import taboolib.common.LifeCycle
import taboolib.common.TabooLib
import taboolib.common.platform.Awake
import taboolib.common.platform.function.getOpenContainers
import taboolib.common.platform.function.info
import taboolib.common.platform.function.pluginId
import taboolib.common.platform.function.registerLifeCycleTask
import taboolib.library.kether.QuestActionParser
import taboolib.module.kether.Kether
import taboolib.module.kether.StandardChannel
import taboolib.module.metrics.charts.DrilldownPie
import top.lanscarlos.vulpecula.Vulpecula
import top.lanscarlos.vulpecula.module.bacikal.parser.BacikalActionParser
import top.lanscarlos.vulpecula.module.bacikal.parser.ComplexActionParser
import top.lanscarlos.vulpecula.module.bacikal.parser.ExceptionalActionParser
import java.util.LinkedList

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal
 *
 * 注册中心
 *
 * @author Lanscarlos
 * @since 2024-11-20 16:33
 */
object BacikalRegistry {

    private val parsers: HashMap<String, BacikalActionParser> = hashMapOf()
    private val exceptionalParsers: LinkedList<ExceptionalActionParser> = LinkedList()

    @Awake(LifeCycle.INIT)
    fun onInit() {
        registerLifeCycleTask(LifeCycle.LOAD, 8) {
            for (parser in parsers.values) {
                registerAction(parser)
            }
            Vulpecula.addMetricsChart(DrilldownPie("actionExtension", ::metricsActionExtension))
            Vulpecula.addMetricsChart(DrilldownPie("extensionAuthor", ::metricsExtensionAuthor))
        }
    }

    fun get(id: String): BacikalActionParser {
        return getOrNull(id) ?: error("Parser $id not found.")
    }

    fun getOrNull(id: String): BacikalActionParser? {
        return parsers[id]
    }

    /**
     * 获取所有已注册的语句解析器 ID
     * */
    fun keys(): Set<String> = parsers.keys

    /**
     * 获取所有已注册的语句解析器
     * */
    fun values(): Collection<BacikalActionParser> = parsers.values

    /**
     * 获取所有已注册的语句解析器键值对
     * */
    fun entries(): Set<Map.Entry<String, BacikalActionParser>> = parsers.entries

    /**
     * 注册异常的语句
     * */
    internal fun getExceptionalParsers(): List<ExceptionalActionParser> = exceptionalParsers

    /**
     * 注册语句解析器
     *
     * @param parser 语句解析器
     * */
    fun registerActionParser(parser: BacikalActionParser) {
        if (parser is ExceptionalActionParser) {
            exceptionalParsers.add(parser)
            return
        }
        parsers[parser.id] = parser

        // 遍历层级并填充父节点
        val newParents = mutableListOf<ComplexActionParser>()
        if (parser.id.contains('.')) {
            // 含层级
            var parent: ComplexActionParser? = null
            val array = parser.id.split('.').dropLast(1)
            for (index in array.indices) {
                val id = array.subList(0, index + 1).joinToString(".")
                val name = array[index]
                val complex = parsers.computeIfAbsent(id) {
                    ComplexActionParser(id, name, emptyArray(), "vulpecula", "Description", parser.source)
                        .also(newParents::add)
                }
                parent?.addActionParser(complex) // 第一次遍历时无父节点
                parent = complex as? ComplexActionParser ?: error("已存在 $id 的末端语句节点!")
            }
            parent?.addActionParser(parser) ?: error("解析 ${parser.id} 的父节点失败")
        }

        when (TabooLib.getCurrentLifeCycle()) {
            LifeCycle.ENABLE,
            LifeCycle.ACTIVE -> {
                // 立刻注册
                for (parent in newParents) {
                    registerAction(parent)
                }
                registerAction(parser)
            }
            else -> {}
        }
    }

    /**
     * 注册语句
     *
     * @param parser 语句解析器
     * */
    private fun registerAction(parser: BacikalActionParser) {
        if (parser.id.contains('.')) {
            // 含层级
            registerAction(
                names = listOf(parser.id.replace('.', '-')),
                namespace = parser.namespace,
                parser = parser
            )
        } else {
            registerAction(
                names = listOf(parser.name).plus(parser.aliases),
                namespace = parser.namespace,
                parser = parser
            )
        }
    }

    /**
     * 注册语句
     *
     * @param names 语句头
     * @param namespace 命名空间
     * @param parser 语句解析器
     * */
    private fun registerAction(names: List<String>, namespace: String, parser: QuestActionParser) {
        // 本地注册
        for (name in names) {
            Kether.scriptRegistry.registerAction(namespace, name, parser)
        }

        // 远程注册
        for (connection in getOpenContainers()) {
            if (connection.name == pluginId) {
                // 过滤自身插件
                continue
            }
            connection.call(StandardChannel.REMOTE_ADD_ACTION, arrayOf(pluginId, names, namespace))
        }
    }

    private fun metricsActionExtension(): Map<String, Map<String, Int>> {
        val outerMap: HashMap<String, HashMap<String, Int>> = hashMapOf()
        val sources = parsers.values.map { it.source }.distinct()
        for (source in sources) {
            val innerMap = outerMap.computeIfAbsent(source.name) { hashMapOf() }
            innerMap.compute(source.version) { _, value ->
                value?.plus(1) ?: 1
            }
        }
        info("Submit data to actionSourceVersion")
        return outerMap
    }

    private fun metricsExtensionAuthor(): Map<String, Map<String, Int>> {
        val outerMap: HashMap<String, HashMap<String, Int>> = hashMapOf()
        val sources = parsers.values.map { it.source }.distinct()
        for (source in sources) {
            for (author in source.authors) {
                val innerMap = outerMap.computeIfAbsent(author) { hashMapOf() }
                innerMap.compute(source.name) { _, value ->
                    value?.plus(1) ?: 1
                }
            }
        }
        info("Submit data to authorToActionSources")
        return outerMap
    }

}
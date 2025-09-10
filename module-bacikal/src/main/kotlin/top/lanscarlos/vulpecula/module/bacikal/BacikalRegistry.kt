package top.lanscarlos.vulpecula.module.bacikal

import taboolib.common.LifeCycle
import taboolib.common.TabooLib
import taboolib.common.io.taboolibPath
import taboolib.common.platform.Awake
import taboolib.common.platform.function.getOpenContainers
import taboolib.common.platform.function.info
import taboolib.common.platform.function.pluginId
import taboolib.common.platform.function.registerLifeCycleTask
import taboolib.common.platform.function.warning
import taboolib.library.kether.QuestActionParser
import taboolib.module.kether.Kether
import taboolib.module.kether.StandardChannel
import taboolib.module.metrics.charts.DrilldownPie
import top.lanscarlos.vulpecula.Vulpecula
import top.lanscarlos.vulpecula.module.bacikal.action.ActionSource
import top.lanscarlos.vulpecula.module.bacikal.action.BuiltInActionSource
import top.lanscarlos.vulpecula.module.bacikal.action.ExternalActionSource
import top.lanscarlos.vulpecula.module.bacikal.parser.BacikalActionParser
import top.lanscarlos.vulpecula.module.bacikal.parser.ComplexActionParser
import top.lanscarlos.vulpecula.module.bacikal.parser.ExceptionalActionParser
import top.lanscarlos.vulpecula.module.bacikal.property.BacikalProperty
import top.lanscarlos.vulpecula.module.bacikal.property.BacikalPropertyResolver
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

    private val sources: HashMap<String, ExternalActionSource> = hashMapOf()
    private val parsers: HashMap<String, BacikalActionParser> = hashMapOf()
    private val properties: HashMap<Class<*>, BacikalProperty<*>> = hashMapOf()
    private val propertyResolvers: HashMap<Class<*>, BacikalPropertyResolver<*>> = hashMapOf()
    private val exceptionalParsers: LinkedList<ExceptionalActionParser> = LinkedList()
    private val sourceByClass: HashMap<Class<*>, ExternalActionSource> = hashMapOf()

    @Awake(LifeCycle.INIT)
    fun onInit() {
        registerLifeCycleTask(LifeCycle.LOAD, 8) {
            for (parser in parsers.values) {
                registerAction(parser)
            }
            for (clazz in properties.keys) {
                val source = sourceByClass[clazz] ?: BuiltInActionSource
                warning("Class ${clazz.name} source not found.")
                registerPropertyResolver(clazz, source, true)
            }
            Vulpecula.addMetricsChart(DrilldownPie("actionExtension", ::metricsActionExtension))
            Vulpecula.addMetricsChart(DrilldownPie("extensionAuthor", ::metricsExtensionAuthor))
        }
    }

    fun getActionSource(name: String): ExternalActionSource {
        return getActionSourceOrNull(name) ?: error("Source $name not found.")
    }

    fun getActionSourceOrNull(name: String): ExternalActionSource? {
        return sources[name]
    }

    fun getActionSourceKeys(): Set<String> = sources.keys

    fun getActionSourceValues(): Collection<ExternalActionSource> = sources.values

    fun getActionSourceEntries(): Set<Map.Entry<String, ExternalActionSource>> = sources.entries

    fun getActionParser(id: String): BacikalActionParser = getActionParserOrNull(id) ?: error("Parser $id not found.")

    fun getActionParserOrNull(id: String): BacikalActionParser? = parsers[id]

    /**
     * 获取所有已注册的语句解析器 ID
     * */
    fun getActionParserKeys(): Set<String> = parsers.keys

    /**
     * 获取所有已注册的语句解析器
     * */
    fun getActionParserValues(): Collection<BacikalActionParser> = parsers.values

    /**
     * 获取所有已注册的语句解析器键值对
     * */
    fun getActionParserEntries(): Set<Map.Entry<String, BacikalActionParser>> = parsers.entries

    fun getPropertyValues(): Collection<BacikalProperty<*>> = properties.values

    fun getPropertyEntries(): Set<Map.Entry<Class<*>, BacikalProperty<*>>> =  properties.entries

    fun getPropertyResolverValues(): Collection<BacikalPropertyResolver<*>> = propertyResolvers.values

    /**
     * 注册异常的语句
     * */
    internal fun getExceptionalParsers(): List<ExceptionalActionParser> = exceptionalParsers

    fun getActionSourceByClass(clazz: Class<*>): ExternalActionSource {
        return sourceByClass[clazz] ?: error("No Source registered for class: ${clazz.name}")
    }

    /**
     * 注册语句来源
     *
     * @param source 语句来源
     * */
    fun registerActionSource(source: ExternalActionSource) {
        sources[source.name] = source
        for (clazz in source.classes.values) {
            sourceByClass[clazz.toClass()] = source
        }
    }

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

    fun registerProperty(typeClass: Class<*>, property: BacikalProperty<*>, source: ActionSource) {
        properties[typeClass] = property
        when (TabooLib.getCurrentLifeCycle()) {
            LifeCycle.ENABLE,
            LifeCycle.ACTIVE -> {
                if (propertyResolvers.contains(typeClass)) {
                    return
                }
                // 立刻注册
                registerPropertyResolver(typeClass, source, true)
            }
            else -> {}
        }
    }

    /**
     * 注册属性
     * */
    fun registerPropertyResolver(clazz: Class<*>, source: ActionSource, shared: Boolean) {
        if (propertyResolvers.containsKey(clazz)) {
            return
        }
        val resolver = BacikalPropertyResolver(clazz.simpleName, clazz, source)
        propertyResolvers[clazz] = resolver

        // 本地注册
        Kether.registeredScriptProperty.computeIfAbsent(clazz) { HashMap() }[resolver.id] = resolver

        if (shared) {
            val remoteName = resolver.bind.name.let {
                if (it.startsWith(taboolibPath)) "@${it.substring(taboolibPath.length)}" else it
            }
            // 远程注册
            for (connection in getOpenContainers()) {
                if (connection.name == pluginId) {
                    continue
                }
                connection.call(
                    StandardChannel.REMOTE_ADD_PROPERTY,
                    arrayOf(pluginId, remoteName, resolver)
                )
            }
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
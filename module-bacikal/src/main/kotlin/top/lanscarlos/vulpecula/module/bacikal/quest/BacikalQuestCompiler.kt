package top.lanscarlos.vulpecula.module.bacikal.quest

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.info
import taboolib.library.kether.*
import taboolib.module.kether.ScriptService
import taboolib.module.metrics.charts.AdvancedPie
import top.lanscarlos.vulpecula.Vulpecula
import top.lanscarlos.vulpecula.module.bacikal.exception.BacikalCompileException
import java.io.File
import java.nio.charset.StandardCharsets

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.quest
 *
 * @author Lanscarlos
 * @since 2024-11-23 12:48
 */
object BacikalQuestCompiler {

    private val statistic: HashMap<String, Map<String, Int>> = hashMapOf()

    @Awake(LifeCycle.LOAD)
    fun onLoad() {
        Vulpecula.addMetricsChart(AdvancedPie("actionUsage", ::metricsActionUsage))
    }

    private fun metricsActionUsage(): Map<String, Int> {
        val map: HashMap<String, Int> = hashMapOf()
        for (data in statistic.values) {
            for ((key, count) in data) {
                map.compute(key) { _, value ->
                    value?.plus(count) ?: count
                }
            }
        }
        info("Submit data to actionUsage")
        return map
    }

    fun compile(source: File, namespace: List<String>): Quest {
        return compile(source.readText(StandardCharsets.UTF_8), source.name, namespace)
    }

    fun compile(source: String, name: String, namespace: List<String>): Quest {
        val loader = BacikalQuestLoader()
        return try {
            val content = if (source.trim().startsWith("def")) source else format(source)
            val id = "bacikal_$name"
            val bytes = content.toByteArray(StandardCharsets.UTF_8)
            val namespace = listOf("vulpecula").plus(namespace).distinct() // 命名空间去重
            val quest = loader.load(ScriptService, id, bytes, namespace)
            statistic[id] = loader.getStatistic()
            quest
        } catch (ex: Exception) {
            throw BacikalCompileException(ex, loader.getParsedMessage(), loader.getUnparseMessage(), loader.getParsedActions())
        }
    }

    private fun format(source: String): String {
        val builder = StringBuilder()
        builder.append("def main = {").append('\n')
        builder.appendIndent(source, 1).append('\n')
        builder.append("}")
        return builder.toString()
    }

    private fun StringBuilder.appendIndent(value: String, indent: Int): StringBuilder {
        val lines = value.trim().split('\n')
        val space = "    ".repeat(indent)
        for ((index, line) in lines.withIndex()) {
            append(space)
            append(line)
            if (index != lines.lastIndex) {
                append('\n')
            }
        }
        return this
    }

}
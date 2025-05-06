package top.lanscarlos.vulpecula.module.bacikal.quest

import taboolib.library.kether.*
import taboolib.module.kether.ScriptService
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

    fun compile(source: File, namespace: List<String>): Quest {
        return compile(source.readText(StandardCharsets.UTF_8), source.name, namespace)
    }

    fun compile(source: String, name: String, namespace: List<String>): Quest {
        val content = if (source.trim().startsWith("def")) source else format(source)
        val loader = BacikalQuestLoader()
        return try {
            loader.load(
                ScriptService,
                "bacikal_$name",
                content.toByteArray(StandardCharsets.UTF_8),
                listOf("vulpecula", *namespace.toTypedArray()).distinct() // 命名空间去重
            )
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
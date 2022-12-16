package top.lanscarlos.vulpecula.internal

import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.utils.coerceListNotNull

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.internal.compiler
 *
 * @author Lanscarlos
 * @since 2022-12-16 13:23
 */
interface ScriptCompiler {

    fun buildSource(): StringBuilder

    fun compileScript()

    fun buildException(value: Any): List<Pair<Set<String>, StringBuilder>> {
        val types = mutableSetOf<String>()
        val builder = StringBuilder()
        val mapping = mutableListOf<Pair<Set<String>, StringBuilder>>(types to builder)

        when (value) {
            is String -> builder.append(value)
            is Map<*, *>,
            is ConfigurationSection -> {
                // 指定了异常类型
                val section = if (value is ConfigurationSection) {
                    value.toMap()
                } else value as Map<*, *>

                types += section["catch"].coerceListNotNull { it?.toString() }
                buildSection(section["handle"] ?: "null", builder)
            }
            is List<*> -> {
                value.mapNotNull { it as? Map<*, *> }.forEach { section ->
                    val catch = section["catch"].coerceListNotNull { it?.toString() }.toSet()
                    val handle = buildSection(section["handle"] ?: "null")
                    mapping += catch to handle
                }
            }
        }

        return mapping
    }

    fun buildSection(section: Any, builder: StringBuilder = StringBuilder()): StringBuilder {
        when (section) {
            is String -> {
                if (builder.isNotEmpty()) {
                    // 此前含有其他内容，换行隔开
                    builder.append('\n')
                }
                builder.append(section)
            }
            is Map<*, *>,
            is ConfigurationSection -> {
                val it = if (section is ConfigurationSection) {
                    section.toMap()
                } else section as Map<*, *>

                val type = it["type"]?.toString() ?: "ke"
                val content = it["content"]?.toString()?.let {
                    buildContent(it, type)
                } ?: return builder

                if (builder.isNotEmpty()) {
                    // 此前含有其他内容，换行隔开
                    builder.append('\n')
                }

                builder.append(content)
            }
            is List<*> -> {
                section.forEach {
                    if (it == null) return@forEach
                    buildSection(it, builder)
                }
            }
        }
        return builder
    }

    fun buildContent(content: String, type: String = "ke"): String? {
        return when (type.lowercase()) {
            "ke", "kether" -> content
            "js", "javascript" -> "js '$content'"
            "ks", "script" -> "vul script run *$content"
//            "kf", "fragment" -> ScriptFragment.link(this, content)
            "kf", "fragment" -> "fragment $content"
            else -> null
        }
    }

    /**
     * 抽取所有字符并清空容器
     * */
    fun StringBuilder.extract(): String {
        val content = this.toString()
        this.clear()
        return content
    }

    fun StringBuilder.appendWithIndent(
        content: String,
        prefix: CharSequence = "",
        suffix: CharSequence = "",
        indentCount: Int = 1,
        indent: String = "    ".repeat(indentCount)
    ) {
        if (indentCount <= 0) {
            append(prefix)
            append(content)
            append(suffix)
        } else {
            content.split('\n').joinTo(
                buffer = this,
                separator = '\n' + indent,
                prefix = indent,
                postfix = suffix
            )
        }
    }
}
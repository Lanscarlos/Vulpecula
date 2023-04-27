package top.lanscarlos.vulpecula.internal

import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.config.DynamicConfig.Companion.bindConfigNode
import top.lanscarlos.vulpecula.utils.coerceListNotNull

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.internal
 *
 * @author Lanscarlos
 * @since 2022-12-16 13:23
 */
interface ScriptCompiler {

    fun buildSource(): StringBuilder

    fun compileScript()

    /**
     * 消除注释
     * */
    fun eraseComment(builder: StringBuilder) {
        if (singleCommentPattern != "[]" && multiCommentPattern != "[]") {
            val pattern = "$singleCommentPattern|$multiCommentPattern".toPattern()
            val matcher = pattern.matcher(builder.extract())
            val buffer = StringBuffer()

            while (matcher.find()) {
                matcher.appendReplacement(buffer, "")
            }
            // 兼容 Github 构建系统
            builder.append(matcher.appendTail(buffer))
        }
    }

    /*
    * 构建变量
    * set $key to $value
    * set $key to {
    *   ...$value
    * }
    * */
    fun compileVariables(builder: StringBuilder, variables: Map<String, String>) {
        variables.forEach { (key, value) ->
            if (value.contains('\n')) {
                // 含有换行
                builder.append("set $key to {\n")
                builder.appendWithIndent(value, suffix = "\n")
                builder.append("}\n")
            } else {
                // 不含有换行
                builder.append("set $key to $value\n")
            }
        }
    }

    /*
    * 构建异常捕捉
    * try {
    *   ...$handle
    * } catch with "...$first_1...|...$first_2..." {
    *   ...$second
    * }
    * */
    fun compileException(builder: StringBuilder, content: String, exception: List<Pair<Set<String>, StringBuilder>>) {
        builder.append("try {\n")
        builder.appendWithIndent(content)
        builder.append("\n}")

        for (it in exception) {
            if (it.second.isEmpty()) continue

            builder.append(" catch ")
            if (it.first.isNotEmpty()) {
                builder.append("with \"")
                builder.append(it.first.joinToString(separator = "|"))
                builder.append("\" ")
            }
            builder.append("{\n")
            builder.appendWithIndent(it.second.toString(), suffix = "\n")
            builder.append("}")
        }
    }

    /*
    * 构建条件体
    * if {
    *   ...$condition
    * } then {
    *   ...$content
    * } else {
    *   ...$deny
    * }
    * */
    fun compileCondition(builder: StringBuilder, content: String, condition: StringBuilder, deny: StringBuilder) {
        builder.append("if {\n")
        builder.appendWithIndent(condition.toString(), suffix = "\n")
        builder.append("} then {\n")
        builder.appendWithIndent(content, suffix = "\n")
        builder.append("}")

        if (deny.isNotEmpty()) {
            builder.append(" else {\n")
            builder.appendWithIndent(deny.toString(), suffix = "\n")
            builder.append("}")
        }
    }

    fun buildVariables(value: Any?, def: Map<String, String>): Map<String, String> {
        return when (value) {
            is ConfigurationSection -> {
                value.toMap().mapNotNull {
                    if (it.value == null) return@mapNotNull null
                    it.key to it.value!!.toString()
                }.toMap()
            }
            is Map<*, *> -> {
                value.mapNotNull {
                    if (it.key == null || it.value == null) return@mapNotNull null
                    it.key!!.toString() to it.value!!.toString()
                }.toMap()
            }
            else -> def
        }
    }

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
            "ks", "script" -> "vul script run *\"$content\""
//            "kf", "fragment" -> ScriptFragment.link(this, content)
//            "kf", "fragment" -> "fragment $content"
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

    companion object {

        val singleCommentPattern by bindConfigNode("script-setting.comment-pattern.single-line") {
            it?.toString() ?: "[]"
        }

        val multiCommentPattern by bindConfigNode("script-setting.comment-pattern.multi-line") {
            it?.toString() ?: "[]"
        }

    }
}
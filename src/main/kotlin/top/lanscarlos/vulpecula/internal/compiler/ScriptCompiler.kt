package top.lanscarlos.vulpecula.internal.compiler

import taboolib.common.platform.function.warning
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.internal.compiler
 *
 * @author Lanscarlos
 * @since 2022-09-06 14:06
 */
abstract class ScriptCompiler(
    protected val id: String,
    protected val config: ConfigurationSection
) {

    val source = StringBuilder()

    abstract fun buildSource()

    fun compileNamespace(buffer: StringBuilder, indent: Int) {
        config["namespace"]?.let { section ->
            val namespace = when (section) {
                is String -> listOf(section)
                is List<*> -> section.mapNotNull { it?.toString() }
                else -> listOf()
            }

            namespace.forEach {
                if (indent > 0) {
                    buffer.appendWithIndent("import \"$it\"", suffix = "\n", indentCount = indent)
                } else {
                    buffer.append("import \"$it\"\n")
                }
            }

            // 结束后使用空行分割
            buffer.append('\n')
        }
    }

    fun compileVariables(buffer: StringBuilder) {
        config.getConfigurationSection("variables")?.let { section ->
            val variables = section.getKeys(false).mapNotNull { key ->
                section.getString(key)?.let { key to it }
            }.toMap()

            variables.forEach { (key, value) ->
                compileMarkPoint(buffer, "variables.$key")
                buffer.append("set $key to {\n")
                buffer.appendWithIndent(value)
                buffer.append("}\n")
            }

            // 结束后使用空行分割
            buffer.append('\n')
        }
    }

    /**
     * 标记检测点
     * */
    open fun compileMarkPoint(buffer: StringBuilder, pointer: String, indent: Int = 0) {
        if (indent <= 0) {
            buffer.append("compiler@checker mark $id $pointer\n")
        } else {
            buffer.appendWithIndent("compiler@mark $id $pointer", suffix = "\n", indentCount = indent)
        }
    }

    /**
     * 清除检测点
     * */
    open fun compileMarkReset(buffer: StringBuilder, indent: Int = 0) {
        if (indent <= 0) {
            buffer.append("compiler@checker reset $id\n")
        } else {
            buffer.appendWithIndent("compiler@checker reset $id", suffix = "\n", indentCount = indent)
        }
    }

    /**
     * 编译部分
     *
     * @param ignoreCondition 无视条件节点
     * */
    protected fun compileSection(buffer: StringBuilder, section: Any, ignoreCondition: Boolean = false): StringBuilder {
        when (section) {
            is String -> {
                if (buffer.isNotEmpty()) {
                    // 此前含有其他内容，换行隔开
                    buffer.append('\n')
                }
                buffer.append(section)
            }
            is Map<*, *>,
            is ConfigurationSection -> {
                val it = if (section is ConfigurationSection) {
                    section.toMap()
                } else section as Map<*, *>

                val type = it["type"]?.toString() ?: "ke"
                val content = it["content"]?.toString()?.let {
                    compileString(it, type)
                } ?: return buffer

                if (buffer.isNotEmpty()) {
                    // 此前含有其他内容，换行隔开
                    buffer.append('\n')
                }

                if (ignoreCondition || "condition" !in it) {
                    // 无视条件节点 或 条件未定义
                    buffer.append(content)
//                    return buffer
                } else {
                    compileCondition(buffer, content, it["condition"]!!, it["deny"])
                }
            }
            is List<*> -> {
                section.forEach {
                    if (it == null) return@forEach
                    compileSection(buffer, it, ignoreCondition)
                }
            }
        }
        return buffer
    }

    /**
     * 编译异常处理
     * */
    protected fun compileCatch(buffer: StringBuilder, content: String, catchSection: Any) {
        buffer.append("try {\n")
        buffer.appendWithIndent(content, suffix = "\n")
        buffer.append("} catch ")

        when (catchSection) {
            is String -> {
                buffer.append("{\n")
                buffer.appendWithIndent(catchSection, suffix = "\n")
                buffer.append("}")
            }
            is Map<*, *>,
            is ConfigurationSection -> {
                val it = if (catchSection is ConfigurationSection) {
                    catchSection.toMap()
                } else catchSection as Map<*, *>

                when (val type = it["catch"]) {
                    is String -> buffer.append("with \"$type\" ")
                    is List<*> -> {
                        buffer.append("with \"")
                        buffer.append(type.distinct().joinToString(separator = "|"))
                        buffer.append("\" ")
                    }
                }

                buffer.append("{\n")
                val handle = compileSection(StringBuilder(), it["handle"] ?: "null", true)
                buffer.appendWithIndent(handle.toString(), suffix = "\n")
                buffer.append("}")
            }
            is List<*> -> {
                val types = mutableSetOf<String>()
                val sb = StringBuilder()

                catchSection.forEach { element ->
                    val meta = element as? Map<*, *> ?: return@forEach

                    if (sb.isEmpty()) {
                        sb.append("if {\n")
                    } else {
                        sb.append("else if {\n")
                    }

                    // 构建判断语句
                    when (val type = meta["catch"]) {
                        is String -> {
                            types += type
                            sb.appendWithIndent("check &exception ==\"$type\"")
                        }
                        is List<*> -> {
                            sb.appendWithIndent("any [", suffix = "\n")
                            type.mapNotNull { it?.toString() }.forEach {
                                types += it
                                sb.appendWithIndent(
                                    "check &exception == \"$it\"",
                                    indentCount = 2,
                                    suffix = "\n"
                                )
                            }
                            sb.appendWithIndent("]", suffix = "\n")
                        }
                        else -> sb.appendWithIndent("true", suffix = "\n")
                    }

                    sb.append("} then {\n")
                    val handle = compileSection(StringBuilder(), meta["handle"] ?: "null", true)
                    sb.appendWithIndent(handle.toString(), suffix = "\n")
                    sb.append("}\n")
                }

                buffer.append("with \"")
                buffer.append(types.joinToString(separator = "|"))
                buffer.append("\" {\n")
                buffer.appendWithIndent(sb.toString(), suffix = "\n")
                buffer.append("}")
            }
            else -> warning("Cannot resolve section \"exception\"!")
        }
    }

    /**
     * 编译条件
     * */
    protected fun compileCondition(buffer: StringBuilder, content: String, conditionSection: Any, denySection: Any? = null): StringBuilder {
        buffer.append("if {\n")
        val condition = compileSection(StringBuilder(), conditionSection, true).toString()
        buffer.appendWithIndent(condition, suffix = "\n")
        buffer.append("} then {\n")
        buffer.appendWithIndent(content, suffix = "\n")
        buffer.append("}")
        if (denySection != null) {
            val deny = compileSection(StringBuilder(), denySection, true).toString()
            buffer.append(" else {\n")
            buffer.appendWithIndent(deny, suffix = "\n")
            buffer.append("}")
        }
        return buffer
    }

    private fun compileString(content: String, type: String = "ke"): String? {
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
    protected fun StringBuilder.extract(): String {
        val content = this.toString()
        this.clear()
        return content
    }

    protected fun StringBuilder.appendWithIndent(
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

        private val markCache = mutableMapOf<String, String>()

        fun peek(id: String): String? = markCache[id]

        fun peekAll(): Map<String, String> = markCache

        fun mark(id: String, pointer: String) {
            markCache[id] = pointer
        }

        fun reset(id: String) {
            markCache.remove(id)
        }

        @KetherParser(["compiler@checker"], namespace = "vulpecula", shared = false)
        fun parse() = scriptParser {
            when (val type = it.nextToken()) {
                "mark" -> {
                    val id = it.nextToken()
                    val pointer = it.nextToken()
                    mark(id, pointer)
                }
                "reset" -> {
                    val id = it.nextToken()
                    reset(id)
                }
            }
            object : ScriptAction<Void>() {
                override fun run(frame: ScriptFrame): CompletableFuture<Void> {
                    return CompletableFuture.completedFuture(null)
                }
            }
        }
    }

}
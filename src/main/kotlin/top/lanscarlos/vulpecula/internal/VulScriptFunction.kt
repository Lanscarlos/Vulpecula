package top.lanscarlos.vulpecula.internal

import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.utils.config.VulConfig

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.internal
 *
 * @author Lanscarlos
 * @since 2022-12-23 13:41
 */
class VulScriptFunction(
    val id: String,
    val wrapper: VulConfig
) : ScriptCompiler {

    val name = "function_$id"

    val parameters by wrapper.readStringList("args")

    val content by wrapper.read("content") {
        if (it != null) buildSection(it) else StringBuilder("null")
    }

    val variables by wrapper.read("variables") { value ->
        buildVariables(value, mapOf())
    }

    val condition by wrapper.read("condition") {
        if (it != null) buildSection(it) else StringBuilder()
    }

    val deny by wrapper.read("deny") {
        if (it != null) buildSection(it) else StringBuilder()
    }

    val exception by wrapper.read("exception") {
        if (it != null) buildException(it) else emptyList()
    }

    var source: StringBuilder = buildSource()

    override fun buildSource(): StringBuilder {
        val builder = StringBuilder()

        /* 构建参数转换 */
        if (parameters.isNotEmpty()) {
            for ((index, arg) in parameters.withIndex()) {
                builder.append("set $arg to &arg$index\n")
            }
            builder.append('\n')
        }

        /* 构建前置变量 */
        if (variables.isNotEmpty()) {
            compileVariables(builder, variables)
            builder.append('\n')
        }

        /* 构建核心语句 */
        builder.append(content.toString())

        /* 构建异常处理 */
        if (exception.isNotEmpty() && (exception.size > 1 || exception.first().second.isNotEmpty())) {
            // 提取先前所有内容
            val content = builder.extract()
            compileException(builder, content, exception)
        }

        /* 构建条件处理 */
        if (condition.isNotEmpty()) {
            // 提取先前所有内容
            val content = builder.extract()
            compileCondition(builder, content, condition, deny)
        }

        /*
        * 收尾
        * 构建方法体
        * def function_$id = {
        *   ...$content
        * }
        * */

        // 提取先前所有内容
        val content = builder.extract()

        // 构建方法体
        builder.append("def $name = {\n")
        builder.appendWithIndent(content, suffix = "\n")
        builder.append("}")

        return builder
    }

    override fun compileScript() {
        source = buildSource()
    }

    /**
     * 对照并尝试更新
     * */
    fun contrast(section: ConfigurationSection) {
        if (wrapper.updateSource(section).isNotEmpty()) {
            compileScript()
        }
    }
}
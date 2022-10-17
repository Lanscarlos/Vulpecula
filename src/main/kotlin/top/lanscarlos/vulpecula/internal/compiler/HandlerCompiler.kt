package top.lanscarlos.vulpecula.internal.compiler

import top.lanscarlos.vulpecula.internal.EventHandler

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.internal.compiler
 *
 * @author Lanscarlos
 * @since 2022-09-06 14:13
 */
class HandlerCompiler(
    val handler: EventHandler
) : ScriptCompiler("handler_${handler.id}", handler.config) {

    override fun buildSource() {
        source.clear()

        // 编译变量
        compileVariables(source)

        // 编译核心代码
        config["handle"]?.let {
            compileMarkPoint(source, "handle")
            compileSection(source, it)

            // 结束后使用空行分割
            source.append('\n')
        } ?: return

        // 编译条件
        config["condition"]?.let { condition ->
            // 提取 source 内的 content
            val content = source.extract()
            compileMarkPoint(source, "condition/deny")
            compileCondition(source, content, condition, config["deny"])
        }

        // 编译异常处理
        config["exception"]?.let { catchSection ->
            val content = source.extract()
            compileMarkPoint(source,"exception")
            compileCatch(source, content, catchSection)
        }

        // 收尾工作，构建方法体
        val content = source.extract()
        source.append("def handler_${handler.hash} = {\n")
        // 编译命名空间
        compileNamespace(source, 1)
        source.appendWithIndent(content, suffix = "\n")
        // 清除检测点
        compileMarkReset(source, indent = 1)
        source.append("}")
    }

}
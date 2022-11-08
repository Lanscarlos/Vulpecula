package top.lanscarlos.vulpecula.internal.compiler

import taboolib.common.platform.function.console
import taboolib.module.kether.Script
import taboolib.module.kether.parseKetherScript
import taboolib.module.lang.sendLang
import top.lanscarlos.vulpecula.internal.EventDispatcher
import top.lanscarlos.vulpecula.internal.EventHandler
import top.lanscarlos.vulpecula.internal.ScriptFragment
import top.lanscarlos.vulpecula.internal.linker.ScriptLinker
import top.lanscarlos.vulpecula.utils.Debug
import top.lanscarlos.vulpecula.utils.Debug.debug

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.internal.compiler
 *
 * @author Lanscarlos
 * @since 2022-09-06 14:11
 */
class DispatcherCompiler(
    val dispatcher: EventDispatcher
) : ScriptCompiler("dispatcher_${dispatcher.id}", dispatcher.config) {

    protected val linker = ScriptLinker(this)
    lateinit var _compiled: Script
    val compiled: Script? get() = if (::_compiled.isInitialized) _compiled else null

    fun compile(): Boolean {
        if (source.isEmpty()) return false
        return try {
            // 取消所有连接
            ScriptFragment.unlink(this)

            // 连接代码段
            val content = source.extract()
            linker.link(source, content)

            // 尝试编译
            _compiled = source.toString().parseKetherScript(listOf("vulpecula"))

            // 检测通过，将缓存导入
            if (dispatcher.handlerCache.isNotEmpty()) {
                dispatcher.handlers.addAll(dispatcher.handlerCache)
                dispatcher.handlerCache.clear()
            }
            true
        } catch (e: Exception) {
            if (peekAll().isEmpty()) {
                console().sendLang("Dispatcher-Load-Failed-Details", "Unknown", "Unknown", e.localizedMessage)
            } else {
                peekAll().forEach { (key, pointer) ->
                    val id = key.split("_").let { it[0] to it[1] }
                    when (id.first) {
                        "dispatcher" -> {
                            console().sendLang("Dispatcher-Load-Failed-Details", id.second, pointer, e.localizedMessage)
                        }
                        "handler" -> {
                            console().sendLang("Handler-Load-Failed-Details", id.second, pointer, e.localizedMessage)
                        }
                        else -> e.printStackTrace()
                    }
                }
            }
            false
        }
    }

    override fun buildSource() {
        source.clear()

        // 编译预处理
        config["pre-handle"]?.let {
            compileMarkPoint(source, "pre-handle")
            compileSection(source, it)

            // 结束后使用空行分割
            source.append('\n')
        }

        // 编译变量
        compileVariables(source)

        val sorted = dispatcher.handlers.toMutableList().also {
            if (dispatcher.handlerCache.isNotEmpty()) {
                it.addAll(dispatcher.handlerCache)
            }
        }.mapNotNull {
            EventHandler.get(it)
        }.sortedByDescending { it.priority }

        sorted.forEach {
            source.append("call handler_${it.hash}\n")
        }

        // 编译尾处理
        config["post-handle"]?.let {
            compileMarkPoint(source, "post-handle")
            compileSection(source, it)
        }

        // 异常处理
        config["exception"]?.let { catchSection ->
            val content = source.extract()
            compileMarkPoint(source,"exception")
            compileCatch(source, content, catchSection)
        }

        // 构建主方法体
        val content = source.extract()
        source.append("def main = {\n")
        // 编译命名空间
        compileNamespace(source, 1)
        source.appendWithIndent(content, suffix = "\n")
        // 清除检测点
        compileMarkReset(source, indent = 1)
        source.append("}")
        source.append("\n\n")

        // 写入 Handler 方法体
        sorted.forEach {
            it.compiler.buildSource()
            source.append(it.compiler.source.toString())
            source.append("\n\n")
        }

        debug(Debug.HIGHEST, "构建脚本：\n$source")
    }

}
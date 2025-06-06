package top.lanscarlos.vulpecula.module.script

import taboolib.common.platform.ProxyCommandSender
import top.lanscarlos.vulpecula.module.bacikal.exception.BacikalRuntimeException
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.function.Function

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.script
 *
 * @author Lanscarlos
 * @since 2025/6/6 17:13
 */
class ScriptFlow(
    private val sender: ProxyCommandSender?,
    private var variables: Map<String, Any> = emptyMap()
) {
    private val scripts = mutableListOf<Script>()
    private var errorHandler: Script? = null

    private var nextPointer: Int = -1
    private var future: CompletableFuture<Any?> = CompletableFuture.completedFuture(null)

    /**
     * 添加要执行的脚本
     */
    fun add(script: Script): ScriptFlow {
        scripts.add(script)
        return this
    }

    /**
     * 设置错误处理脚本
     */
    fun onError(script: Script): ScriptFlow {
        errorHandler = script
        return this
    }

    /**
     * 执行脚本流
     */
    fun execute(): CompletableFuture<Any?> {
        if (scripts.isEmpty()) {
            return CompletableFuture.completedFuture(null)
        }

        nextPointer = 0
        future = process(future)
        return future
    }

    private fun process(source: CompletableFuture<*>): CompletableFuture<Any?> {
        return source.thenCompose { result ->
            // TODO 这里可以执行上一个 Task 的后置处理
            val task = nextTask()?.future ?: return@thenCompose CompletableFuture.completedFuture(result)
            // TODO 这里可以执行该 Task 的前置处理
            process(task)
        }
    }

    private fun nextTask(): ScriptTask? {
        val script = scripts.getOrNull(nextPointer++) ?: return null
        val task = script.run(sender = sender, emptyList(), variables = variables)
        task.onComplete {
            // 更新变量
            this.variables = task.variables()
        }
        return task
    }
}
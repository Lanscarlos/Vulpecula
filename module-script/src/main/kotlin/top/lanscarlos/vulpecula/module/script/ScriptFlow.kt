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
    private val variables: Map<String, Any> = emptyMap()
) {
    private val scripts = mutableListOf<Script>()
    private var errorHandler: Script? = null

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

        var future = CompletableFuture<Any?>()

        // 递归执行脚本
        fun executeNext(index: Int, lastResult: Any?) {
            if (index >= scripts.size) {
                future.complete(lastResult)
                return
            }

            val script = scripts[index]
            val task = script.run(
                sender = sender,
                args = listOf(lastResult),
                variables = variables,
                onSuccess = { result ->
                    executeNext(index + 1, result)
                },
                onFailure = { ex ->
                    if (errorHandler != null) {
                        // 执行错误处理脚本
                        errorHandler!!.run(
                            sender = sender,
                            args = listOf(ex),
                            variables = variables,
                            onSuccess = { result ->
                                executeNext(index + 1, result)
                            },
                            onFailure = { errorEx ->
                                future.completeExceptionally(errorEx)
                            }
                        )
                    } else {
                        future.completeExceptionally(ex)
                    }
                }
            )
        }

        // 开始执行第一个脚本
        executeNext(0, null)

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
        TODO()
    }
}
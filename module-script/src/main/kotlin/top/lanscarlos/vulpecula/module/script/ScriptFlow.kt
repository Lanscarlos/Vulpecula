package top.lanscarlos.vulpecula.module.script

import taboolib.common.platform.ProxyCommandSender
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

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
    private val preprocessMap = mutableMapOf<Script, Consumer<ScriptTask>>()
    private val postprocessMap = mutableMapOf<Script, Consumer<ScriptTask>>()

    private var nextPointer: Int = -1
    private lateinit var currentTask: ScriptTask
    private var future: CompletableFuture<Any?> = CompletableFuture.completedFuture(null)

    /**
     * 设置最后一个 Script 的前置处理逻辑
     */
    fun preprocess(preprocess: Consumer<ScriptTask>): ScriptFlow {
        require(scripts.isNotEmpty()) { "Scripts list is empty. Cannot set pre-process." }
        preprocessMap[scripts.last()] = preprocess
        return this
    }

    /**
     * 设置最后一个 Script 的后置处理逻辑
     */
    fun postprocess(postprocess: Consumer<ScriptTask>): ScriptFlow {
        require(scripts.isNotEmpty()) { "Scripts list is empty. Cannot set post-process." }
        postprocessMap[scripts.last()] = postprocess
        return this
    }

    /**
     * 添加要执行的脚本
     */
    fun add(script: Script): ScriptFlow {
        scripts.add(script)
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
            // 执行上一个 Task 的后置处理
            if (::currentTask.isInitialized) {
                postprocessMap[scripts.getOrNull(nextPointer - 2)]?.accept(currentTask)
            }
            val task = nextTask() ?: return@thenCompose CompletableFuture.completedFuture(result)
            // 执行该 Task 的前置处理
            preprocessMap[scripts.getOrNull(nextPointer - 1)]?.accept(task)
            process(task.future)
        }
    }

    private fun nextTask(): ScriptTask? {
        val script = scripts.getOrNull(nextPointer++) ?: return null
        val task = script.run(sender = sender, args = emptyList(), variables = variables)
        task.onSuccess {
            // 更新变量
            this.variables = task.variables()
        }
        this.currentTask = task
        return task
    }
}
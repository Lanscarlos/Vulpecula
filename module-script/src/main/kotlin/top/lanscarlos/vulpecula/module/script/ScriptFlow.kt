package top.lanscarlos.vulpecula.module.script

import taboolib.common.platform.ProxyCommandSender
import top.lanscarlos.vulpecula.module.bacikal.exception.BacikalRuntimeException
import top.lanscarlos.vulpecula.module.script.exception.ScriptExecuteException
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
    private var onFailure: Consumer<BacikalRuntimeException>? = null
    private val preprocessMap = mutableMapOf<Script, Consumer<Script>>()
    private val postprocessMap = mutableMapOf<Script, Consumer<ScriptTask>>()

    private var nextPointer: Int = -1
    private var isTerminated: Boolean = false
    private lateinit var currentTask: ScriptTask
    private var future: CompletableFuture<Any?> = CompletableFuture.completedFuture(null)

    /**
     * 终止脚本流
     */
    fun terminate() {
        isTerminated = true
    }

    /**
     * 设置最后一个 Script 的前置处理逻辑
     */
    fun preprocess(preprocess: Consumer<Script>): ScriptFlow {
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
     * 设置当脚本执行失败时的处理逻辑。
     * 当 [ScriptFlow] 中的某个脚本抛出 [BacikalRuntimeException] 异常时，
     * 将调用此方法设置的处理逻辑来处理异常。
     *
     * @param func 处理异常的函数，接收 [BacikalRuntimeException] 参数
     */
    fun onFailure(func: Consumer<BacikalRuntimeException>) {
        onFailure = func
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

        try {
            nextPointer = 0
            future = process(future)
        } catch (e: Exception) {
            throw ScriptExecuteException(scripts[nextPointer], e)
        }
        return future
    }

    private fun process(source: CompletableFuture<*>): CompletableFuture<Any?> {
        return source.thenCompose { result ->
            // 执行上一个 Task 的后置处理
            if (::currentTask.isInitialized) {
                postprocessMap[scripts.getOrNull(nextPointer - 1)]?.accept(currentTask)
            }
            val task = nextTask() ?: return@thenCompose CompletableFuture.completedFuture(result)
            process(task.future)
        }
    }

    private fun nextTask(): ScriptTask? {
        if (isTerminated) {
            return null
        }
        val script = scripts.getOrNull(nextPointer++) ?: return null
        preprocessMap[script]?.accept(script) // 前置处理
        val task = script.run(sender = sender, args = emptyList(), variables = variables)
        task.onSuccess {
            // 更新变量
            this.variables = task.variables()
        }
        task.onFailure {
            terminate()
            onFailure?.accept(it)
        }
        this.currentTask = task
        return task
    }
}
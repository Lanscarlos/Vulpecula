package top.lanscarlos.vulpecula.module.script

import top.lanscarlos.vulpecula.module.bacikal.exception.QuestRuntimeException
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.function.Function

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.script
 *
 * 代表一个正在运行中的脚本任务
 *
 * @author Lanscarlos
 * @since 2025/4/27 14:02
 */
interface ScriptTask {

    val pid: Long

    val script: Script

    val startTime: Long

    val future: CompletableFuture<Any?>

    val isDone: Boolean

    fun variables(): Map<String, Any>

    /**
     * 停止任务
     * */
    fun stop()

    fun onSuccess(func: Consumer<Any?>): ScriptTask

    fun onFailure(func: Function<QuestRuntimeException, Any?>): ScriptTask

    fun getNow(): Any?

}
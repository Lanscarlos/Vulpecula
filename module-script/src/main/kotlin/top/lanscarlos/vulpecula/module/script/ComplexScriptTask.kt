package top.lanscarlos.vulpecula.module.script

import io.foldright.cffu.CompletableFutureUtils
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.script
 *
 * @author Lanscarlos
 * @since 2025-05-18 11:04
 */
class ComplexScriptTask(
    override val pid: Long,
    override val script: Script,
    val tasks: List<ScriptTask>,
) : ScriptTask {

    val futures = tasks.map { it.future }

    override val future: CompletableFuture<List<Any?>> = CompletableFutureUtils.allResultsOf(*futures.toTypedArray())

    override val startTime: Long = System.currentTimeMillis()

    override val isDone: Boolean
        get() = tasks.all { it.isDone }

    override fun terminate() {
        for (task in tasks) {
            task.terminate()
        }
    }
}
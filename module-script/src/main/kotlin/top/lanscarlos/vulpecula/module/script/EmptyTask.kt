package top.lanscarlos.vulpecula.module.script

import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.script
 *
 * 空任务
 *
 * @author Lanscarlos
 * @since 2025/5/22 9:25
 */
class EmptyTask(override val script: Script) : ScriptTask {

    override val pid: Long = -1L

    override val future: CompletableFuture<out Any?> = CompletableFuture.completedFuture(null)

    override val startTime: Long = System.currentTimeMillis()

    override val isDone: Boolean = true

    override fun terminate() {
    }
}
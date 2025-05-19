package top.lanscarlos.vulpecula.module.script

import taboolib.module.kether.ScriptContext
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.script
 *
 * @author Lanscarlos
 * @since 2025/4/27 14:10
 */
class DefaultScriptTask(
    override val pid: Long,
    override val script: Script,
    val context: ScriptContext,
    override var future: CompletableFuture<Any?>,
    override val startTime: Long
) : ScriptTask {

    override val isDone: Boolean
        get() = future.isDone

    override fun terminate() {
        context.terminate()
    }

}
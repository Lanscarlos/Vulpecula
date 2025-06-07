package top.lanscarlos.vulpecula.module.script

import taboolib.module.kether.ScriptContext
import top.lanscarlos.vulpecula.module.bacikal.exception.BacikalRuntimeException
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.function.Function

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

    override fun variables(): Map<String, Any> {
        return context.rootFrame().variables().toMap()
    }

    override fun stop() {
        context.terminate()
    }

    override fun onSuccess(func: Consumer<Any?>): ScriptTask {
        future = future.thenApply { func.accept(it) }
        return this
    }

    override fun onFailure(func: Function<BacikalRuntimeException, Any?>): ScriptTask {
        future = future.exceptionally {
            val ex = it.cause as BacikalRuntimeException
            return@exceptionally func.apply(ex)
        }
        return this
    }

    override fun getNow(): Any? {
        return future.getNow(null)
    }

}
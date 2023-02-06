package top.lanscarlos.vulpecula.kether.live

import taboolib.library.kether.ParsedAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.run
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.live
 *
 * @author Lanscarlos
 * @since 2022-11-17 00:19
 */
class CollectionLiveData(
    val value: Any
) : LiveData<Collection<*>> {

    override fun get(frame: ScriptFrame, def: Collection<*>): CompletableFuture<Collection<*>> {
        return getOrNull(frame).thenApply { it ?: def }
    }

    override fun getOrNull(frame: ScriptFrame): CompletableFuture<Collection<*>?> {
        val future = if (value is ParsedAction<*>) {
            frame.run(value)
        } else CompletableFuture.completedFuture(value)

        return future.thenApply {
            when (it) {
                is Collection<*> -> it
                is Array<*> -> it.map { el -> el }
                else -> if (it != null) setOf(it) else null
            }
        }
    }
}
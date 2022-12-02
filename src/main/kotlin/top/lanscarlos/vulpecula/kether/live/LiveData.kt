package top.lanscarlos.vulpecula.kether.live

import taboolib.module.kether.ScriptFrame
import top.lanscarlos.vulpecula.utils.thenTake
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicInteger

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.live
 *
 * 用于兼容 Kether 语句读取到的不同类型的数据
 *
 * @author Lanscarlos
 * @since 2022-11-10 15:03
 */
interface LiveData<T: Any> {

    fun get(frame: ScriptFrame, def: T): CompletableFuture<T>

    fun getOrNull(frame: ScriptFrame): CompletableFuture<T?>

    @Deprecated("Use List<CompletedFuture<*>>.thenTake()")
    fun <R> thenApply(frame: ScriptFrame, def: T, vararg futures: CompletableFuture<*>, func: T.(List<Any?>) -> R): CompletableFuture<R> {
        val future = CompletableFuture<R>()
        futures.toList().thenTake().thenAccept { parameters ->
            this.get(frame, def).thenAccept {
                future.complete(func(it, parameters))
            }
        }
        return future
    }

    @Deprecated("Use List<CompletedFuture<*>>.thenTake()")
    fun <R> thenApplyOrNull(frame: ScriptFrame, vararg futures: CompletableFuture<*>, func: T?.(List<Any?>) -> R): CompletableFuture<R> {
        val future = CompletableFuture<R>()
        futures.toList().thenTake().thenAccept { parameters ->
            this.getOrNull(frame).thenAccept {
                future.complete(func(it, parameters))
            }
        }
        return future
    }
}
package top.lanscarlos.vulpecula.kether.live

import taboolib.module.kether.ScriptFrame
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

    fun <R> thenApply(frame: ScriptFrame, def: T, vararg futures: CompletableFuture<*>, func: T.(List<Any?>) -> R): CompletableFuture<R> {
        val future = CompletableFuture<R>()
        wait(*futures).thenAccept { parameters ->
            this.get(frame, def).thenAccept {
                future.complete(func(it, parameters))
            }
        }
        return future
    }

    fun <R> thenApplyOrNull(frame: ScriptFrame, vararg futures: CompletableFuture<*>, func: T?.(List<Any?>) -> R): CompletableFuture<R> {
        val future = CompletableFuture<R>()
        wait(*futures).thenAccept { parameters ->
            this.getOrNull(frame).thenAccept {
                future.complete(func(it, parameters))
            }
        }
        return future
    }

    private fun wait(vararg futures: CompletableFuture<*>): CompletableFuture<List<Any?>> {
        val parameters = CompletableFuture<List<Any?>>()
        if (futures.isEmpty()) {
            // 队列为空
            parameters.complete(listOf())
        } else if (futures.size == 1) {
            // 队列仅有一个
            val future = futures.first()
            if (future.isDone) {
                parameters.complete(listOf(future.getNow(null)))
            } else {
                future.thenAccept { parameters.complete(listOf(it)) }
            }
        } else {
            // 多个
            val counter = AtomicInteger(0)
            for (it in futures) {
                if (it.isDone) {
                    val count = counter.incrementAndGet()

                    // 判断 futures 是否全部执行完毕
                    if (count >= futures.size) {
                        parameters.complete(futures.map { it.getNow(null) })
                    }
                } else {
                    it.thenRun {
                        val count = counter.incrementAndGet()

                        // 判断 futures 是否全部执行完毕
                        if (count >= futures.size) {
                            parameters.complete(futures.map { it.getNow(null) })
                        }
                    }
                }
            }
        }
        return parameters
    }
}
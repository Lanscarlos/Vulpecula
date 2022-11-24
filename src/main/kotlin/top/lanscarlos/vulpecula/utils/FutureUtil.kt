package top.lanscarlos.vulpecula.utils

import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicInteger

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.utils
 *
 * @author Lanscarlos
 * @since 2022-11-24 23:14
 */

/**
 * 将一系列 CompletableFuture 转成参数
 * */
fun List<CompletableFuture<*>?>.thenTake(): CompletableFuture<List<Any?>> {
    val parameters = CompletableFuture<List<Any?>>()
    if (this.isEmpty()) {
        // 队列为空
        parameters.complete(listOf())
    } else if (this.size == 1) {
        // 队列仅有一个
        val future = this.first()
        if (future == null || future.isDone) {
            parameters.complete(listOf(future?.getNow(null)))
        } else {
            future.thenAccept { parameters.complete(listOf(it)) }
        }
    } else {
        // 多个
        val counter = AtomicInteger(0)
        for (it in this) {
            if (it == null || it.isDone) {
                val count = counter.incrementAndGet()

                // 判断 futures 是否全部执行完毕
                if (count >= this.size) {
                    parameters.complete(this.map { it?.getNow(null) })
                }
            } else {
                it.thenRun {
                    val count = counter.incrementAndGet()

                    // 判断 futures 是否全部执行完毕
                    if (count >= this.size) {
                        parameters.complete(this.map { it?.getNow(null) })
                    }
                }
            }
        }
    }
    return parameters
}
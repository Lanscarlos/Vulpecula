package top.lanscarlos.vulpecula.bacikal.action.target.filter

import taboolib.common5.cbool
import top.lanscarlos.vulpecula.bacikal.action.target.ActionTarget
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.target.filter
 *
 * @author Lanscarlos
 * @since 2023-03-23 12:45
 */
object ActionTargetFilterForeach : ActionTarget.Resolver {

    override val name: Array<String> = arrayOf("foreach", "each")

    override fun resolve(reader: ActionTarget.Reader): ActionTarget.Handler<out Any?> {
        return reader.transfer {
            combineOf(
                source(),
                optional("by", then = literal(), def = "it"),
                trim("then", then = action())
            ) { target, name, body ->

                val newTarget = ConcurrentLinkedQueue<Any>()

                val size = target.size
                // 原子计数器
                val counter = AtomicInteger(0)
                val future = CompletableFuture<MutableCollection<Any>>()

                for (element in target) {
                    val newFrame = this.newFrame(body)
                    newFrame.variables().set(name, element)
                    val result = newFrame.run<Any?>()

                    if (result.isDone) {
                        // 根据结果加入新目标数组
                        if (result.getNow(false)?.cbool == true) {
                            newTarget += element
                        }

                        val count = counter.incrementAndGet()
                        // 判断 target 是否全部遍历完毕
                        if (!future.isDone && count >= size) {
                            future.complete(newTarget)
                        }
                    } else {
                        result.thenAccept { filtered ->

                            // 根据结果加入新目标数组
                            if (filtered?.cbool == true) {
                                newTarget.offer(element)
                            }

                            val count = counter.incrementAndGet()
                            // 判断 target 是否全部遍历完毕
                            if (!future.isDone && count >= size) {
                                future.complete(newTarget)
                            }
                        }
                    }
                }

                future
            }
        }
    }
}
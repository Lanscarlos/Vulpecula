package top.lanscarlos.vulpecula.bacikal.action.target

import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicInteger

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.target
 *
 * @author Lanscarlos
 * @since 2023-03-22 20:32
 */
object ActionTargetForeach : ActionTarget.Resolver {

    override val name: Array<String> = arrayOf("foreach")

    override fun resolve(reader: ActionTarget.Reader): ActionTarget.Handler<out Any?> {
        return reader.transfer {
            combineOf(
                source(),
                optional("by", then = literal(), def = "it"),
                trim("then", then = action())
            ) { target, name, body ->

                val size = target.size
                // 原子计数器
                val counter = AtomicInteger(0)
                val future = CompletableFuture<MutableCollection<Any>>()

                for (element in target) {
                    val newFrame = this.newFrame(body)
                    newFrame.variables().set(name, element)
                    newFrame.run<Any?>().thenRun {
                        val count = counter.incrementAndGet()
                        // 判断 target 是否全部遍历完毕
                        if (!future.isDone && count >= size) {
                            future.complete(target)
                        }
                    }
                }

                future
            }
        }
    }
}
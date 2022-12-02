package top.lanscarlos.vulpecula.kether.action.target

import taboolib.library.kether.QuestReader
import top.lanscarlos.vulpecula.kether.live.readCollection
import top.lanscarlos.vulpecula.utils.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicInteger

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.target
 *
 * @author Lanscarlos
 * @since 2022-11-17 00:30
 */
object TargetForEachHandler : ActionTarget.Reader {

    override val name: Array<String> = arrayOf("foreach")

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionTarget.Handler {
        val source = if (isRoot) reader.readCollection() else null
        val token = if (reader.hasNextToken("by")) reader.nextToken() else "it"
        val body = reader.nextBlock()

        return acceptHandlerFuture(source) { collection ->

            val counter = AtomicInteger(0)
            val future = CompletableFuture<MutableCollection<Any>>()

            for (element in collection) {
                this.newFrame(body).also {
                    // 覆盖变量
                    it.variables().set(token, element)
                    it.run<Any?>().thenRun {
                        val count = counter.incrementAndGet()
                        // 判断 collection 是否全部遍历完毕
                        if (!future.isDone && count >= collection.size) {
                            future.complete(collection)
                        }
                    }
                }
            }

            return@acceptHandlerFuture future
        }
    }
}
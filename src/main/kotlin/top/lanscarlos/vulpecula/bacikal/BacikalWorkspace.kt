package top.lanscarlos.vulpecula.bacikal

import kotlinx.coroutines.*
import taboolib.common.platform.function.info
import taboolib.module.kether.*
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.utils.buildParser
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal
 *
 * @author Lanscarlos
 * @since 2023-02-26 13:32
 */
object BacikalWorkspace {

    private val coroutineScope by lazy { CoroutineScope(Dispatchers.Default) }

    fun launch(block: suspend CoroutineScope.() -> Unit) {
        coroutineScope.launch(block = block)
    }

    fun shutdown() {
//        coroutineScope.cancel()
    }

    fun runActions() {

    }

//    @KetherParser(["output"])
//    fun parser() = scriptParser {
//        val next = it.nextParsedAction()
//        actionTake {
//            val future = CompletableFuture<Any?>()
//            info("before")
//            launch {
//                info("inner")
//                val result = run(next).join()
//                info("result -> $result")
//                future.complete(result)
//            }
//            info("after")
//            future
//        }
//    }
}
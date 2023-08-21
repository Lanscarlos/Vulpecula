package top.lanscarlos.vulpecula.bacikal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal
 *
 * @author Lanscarlos
 * @since 2023-08-20 23:05
 */
class CoroutinesBacikalExecutor(quest: BacikalQuest) : BacikalExecutor {

    override fun runActions(): CompletableFuture<Any?> {
        val future = CompletableFuture<Any?>()
        scope.launch {
            TODO()
        }
        return future
    }

    companion object {
        private val scope by lazy { CoroutineScope(Dispatchers.Default) }
    }
}
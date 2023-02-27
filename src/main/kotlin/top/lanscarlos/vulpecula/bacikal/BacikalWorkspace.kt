package top.lanscarlos.vulpecula.bacikal

import kotlinx.coroutines.*
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.info

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal
 *
 * @author Lanscarlos
 * @since 2023-02-26 13:32
 */
object Workspace {

    internal val coroutineScope by lazy { CoroutineScope(Dispatchers.Default) }

    fun shutdown() {
        coroutineScope.cancel()
    }

    fun test() {
        val coroutineScope = CoroutineScope(Dispatchers.Default)
        coroutineScope.launch {
            info("test-1")
            delay(1000)
            info("test-2")
            delay(1000)
            info("test-3")
            delay(1000)
            info("test-4")
            delay(1000)
        }.invokeOnCompletion {
            info("completed.")
        }
    }
}
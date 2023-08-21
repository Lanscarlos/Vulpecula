package top.lanscarlos.vulpecula.bacikal

import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal
 *
 * @author Lanscarlos
 * @since 2023-08-20 21:30
 */
interface BacikalExecutor {

    fun runActions(): CompletableFuture<Any?>

}
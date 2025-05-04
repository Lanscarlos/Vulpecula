package top.lanscarlos.vulpecula.module.bacikal.parser

import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.parser
 *
 * @author Lanscarlos
 * @since 2024-11-20 14:03
 */
interface BacikalAction<T>  {

    fun execute(frame: BacikalFrame): CompletableFuture<T>

}
package top.lanscarlos.vulpecula.module.bacikal.parser

import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.parser
 *
 * @author Lanscarlos
 * @since 2025/6/28
 */
object FrameAction : BacikalAction<BacikalFrame> {
    override fun execute(frame: BacikalFrame): CompletableFuture<BacikalFrame> {
        return CompletableFuture.completedFuture(frame)
    }
}
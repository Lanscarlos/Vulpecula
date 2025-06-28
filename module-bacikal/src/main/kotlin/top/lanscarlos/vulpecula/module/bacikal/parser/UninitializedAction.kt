package top.lanscarlos.vulpecula.module.bacikal.parser

import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.parser
 *
 * 未初始化参数, 作占位用
 *
 * @author Lanscarlos
 * @since 2025/6/28
 */
object UninitializedAction : BacikalAction<Unit> {
    override fun execute(frame: BacikalFrame): CompletableFuture<Unit> {
        return CompletableFuture.completedFuture(Unit)
    }
}
package top.lanscarlos.vulpecula.module.bacikal.parser

import taboolib.library.kether.ParsedAction
import top.lanscarlos.vulpecula.common.applicative.Applicative
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.parser
 *
 * @author Lanscarlos
 * @since 2025/6/28
 */
class ApplicativeAction<T: Any>(val source: ParsedAction<*>, val applicative: Applicative<T>) : BacikalAction<T> {
    override fun execute(frame: BacikalFrame): CompletableFuture<T> {
        return frame.runAction(source).thenApply(applicative::convertOrNull)
    }
}
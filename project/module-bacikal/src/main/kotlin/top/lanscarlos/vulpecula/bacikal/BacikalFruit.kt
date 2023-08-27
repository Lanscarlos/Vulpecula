package top.lanscarlos.vulpecula.bacikal

import taboolib.library.kether.QuestAction
import taboolib.module.kether.ScriptFrame
import top.lanscarlos.vulpecula.bacikal.parser.BacikalFrame
import top.lanscarlos.vulpecula.bacikal.parser.DefaultFrame
import java.util.concurrent.CompletableFuture
import java.util.function.Function

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal
 *
 * @author Lanscarlos
 * @since 2023-08-21 10:15
 */
class BacikalFruit<T>(private val func: Function<BacikalFrame, CompletableFuture<T>>) : QuestAction<T>() {
    override fun process(frame: ScriptFrame): CompletableFuture<T> {
        return func.apply(DefaultFrame(frame))
    }
}
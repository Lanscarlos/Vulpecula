package top.lanscarlos.vulpecula.bacikal.seed

import taboolib.library.kether.ParsedAction
import taboolib.module.kether.ScriptFrame
import top.lanscarlos.vulpecula.bacikal.BacikalReader
import top.lanscarlos.vulpecula.bacikal.BacikalSeed
import java.util.concurrent.CompletableFuture
import java.util.function.BiFunction

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.seed
 *
 * @author Lanscarlos
 * @since 2023-08-21 15:15
 */
class DefaultBacikalSeed<T>(val transfer: BiFunction<ScriptFrame, Any?, T>) : BacikalSeed<T> {

    private lateinit var action: ParsedAction<*>

    override val isAccepted: Boolean
        get() = ::action.isInitialized

    override fun accept(reader: BacikalReader) {
        action = reader.readAction()
    }

    override fun accept(frame: ScriptFrame): CompletableFuture<T> {
        return action.process(frame).thenApply {
            transfer.apply(frame, it)
        }
    }

}
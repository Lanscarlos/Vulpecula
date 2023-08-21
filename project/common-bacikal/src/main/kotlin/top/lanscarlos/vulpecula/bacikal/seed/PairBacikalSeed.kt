package top.lanscarlos.vulpecula.bacikal.seed

import taboolib.module.kether.ScriptFrame
import top.lanscarlos.vulpecula.bacikal.BacikalReader
import top.lanscarlos.vulpecula.bacikal.combineFuture
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.seed
 *
 * @author Lanscarlos
 * @since 2023-08-21 20:00
 */
class PairBacikalSeed<T, R>(val first: BacikalSeed<T>, val second: BacikalSeed<R>) : BacikalSeed<Pair<T, R>> {

    override val isAccepted: Boolean
        get() = first.isAccepted && second.isAccepted

    override fun accept(reader: BacikalReader) {
        first.accept(reader)
        second.accept(reader)
    }

    override fun accept(frame: ScriptFrame): CompletableFuture<Pair<T, R>> {
        return combineFuture(
            first.accept(frame),
            second.accept(frame)
        ).thenApply {
            it.t1 to it.t2
        }
    }
}
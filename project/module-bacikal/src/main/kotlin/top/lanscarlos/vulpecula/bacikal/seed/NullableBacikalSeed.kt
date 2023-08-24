package top.lanscarlos.vulpecula.bacikal.seed

import taboolib.module.kether.ScriptFrame
import top.lanscarlos.vulpecula.bacikal.BacikalReader
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.seed
 *
 * @author Lanscarlos
 * @since 2023-08-21 18:03
 */
class NullableBacikalSeed<T>(val seed: BacikalSeed<T>) : BacikalSeed<T?> {

    override val isAccepted: Boolean
        get() = seed.isAccepted

    override fun accept(reader: BacikalReader) {
        seed.accept(reader)
    }

    override fun accept(frame: ScriptFrame): CompletableFuture<T?> {
        return seed.accept(frame).thenApply { it }
    }
}
package top.lanscarlos.vulpecula.bacikal.seed

import taboolib.module.kether.ScriptFrame
import top.lanscarlos.vulpecula.bacikal.BacikalReader
import top.lanscarlos.vulpecula.bacikal.BacikalSeed
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.seed
 *
 * @author Lanscarlos
 * @since 2023-08-21 17:43
 */
class OptionalBacikalSeed<T>(val seed: BacikalSeed<T>, val expect: Array<out String>, val def: T) : BacikalSeed<T> {

    override val isAccepted: Boolean
        get() = seed.isAccepted

    override fun accept(reader: BacikalReader) {
        if (reader.hasToken(*expect)) {
            seed.accept(reader)
        }
    }

    override fun accept(frame: ScriptFrame): CompletableFuture<T> {
        return if (isAccepted) {
            seed.accept(frame)
        } else {
            CompletableFuture.completedFuture(def)
        }
    }
}
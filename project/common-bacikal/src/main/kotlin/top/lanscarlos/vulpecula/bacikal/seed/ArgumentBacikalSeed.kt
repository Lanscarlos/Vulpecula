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
 * @since 2023-08-21 17:59
 */
class ArgumentBacikalSeed<T>(val seed: BacikalSeed<T>, val prefix: Array<out String>, val def: T) : BacikalSeed<T> {

    override val isAccepted: Boolean
        get() = seed.isAccepted

    fun accept(prefix: String, reader: BacikalReader) {
        if (prefix in this.prefix) {
            seed.accept(reader)
        }
    }

    override fun accept(reader: BacikalReader) {
    }

    override fun accept(frame: ScriptFrame): CompletableFuture<T> {
        return if (seed.isAccepted) {
            seed.accept(frame)
        } else {
            CompletableFuture.completedFuture(def)
        }
    }
}
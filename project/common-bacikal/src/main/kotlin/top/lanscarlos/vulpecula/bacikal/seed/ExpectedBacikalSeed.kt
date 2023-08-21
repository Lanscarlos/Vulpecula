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
 * @since 2023-08-21 17:39
 */
class ExpectedBacikalSeed<T>(val seed: BacikalSeed<T>, val expect: Array<out String>) : BacikalSeed<T> {

    override val isAccepted: Boolean
        get() = seed.isAccepted

    override fun accept(reader: BacikalReader) {
        reader.expectToken(*expect)
        seed.accept(reader)
    }

    override fun accept(frame: ScriptFrame): CompletableFuture<T> {
        return seed.accept(frame)
    }
}
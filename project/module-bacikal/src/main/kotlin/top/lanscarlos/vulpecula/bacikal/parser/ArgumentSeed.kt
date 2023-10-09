package top.lanscarlos.vulpecula.bacikal.parser

import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.seed
 *
 * @author Lanscarlos
 * @since 2023-08-21 17:59
 */
class ArgumentSeed<T>(val seed: BacikalSeed<T>, val prefix: Array<out String>, val def: T) : BacikalSeed<T> {

    override val isAccepted: Boolean
        get() = seed.isAccepted

    /**
     * @return 若前缀匹配则返回 true
     * */
    fun accept(prefix: String, reader: BacikalReader): Boolean {
        if (isAccepted) {
            return false
        }
        if (prefix in this.prefix) {
            seed.accept(reader)
            return true
        }
        return false
    }

    override fun accept(reader: BacikalReader) {
    }

    override fun accept(frame: BacikalFrame): CompletableFuture<T> {
        return if (seed.isAccepted) {
            seed.accept(frame)
        } else {
            CompletableFuture.completedFuture(def)
        }
    }
}
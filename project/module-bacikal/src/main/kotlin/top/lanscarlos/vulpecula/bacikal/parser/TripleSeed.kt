package top.lanscarlos.vulpecula.bacikal.parser

import top.lanscarlos.vulpecula.bacikal.combineFuture
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.parser
 *
 * @author Lanscarlos
 * @since 2023-12-27 15:22
 */
class TripleSeed<T, R, S>(
    val first: BacikalSeed<T>,
    val second: BacikalSeed<R>,
    val third: BacikalSeed<S>
) : BacikalSeed<Triple<T, R, S>> {

    override val isAccepted: Boolean
        get() = first.isAccepted && second.isAccepted && third.isAccepted

    override fun accept(reader: BacikalReader) {
        first.accept(reader)
        second.accept(reader)
        third.accept(reader)
    }

    override fun accept(frame: BacikalFrame): CompletableFuture<Triple<T, R, S>> {
        return combineFuture(
            first.accept(frame),
            second.accept(frame),
            third.accept(frame)
        ).thenApply {
            Triple(it.t1, it.t2, it.t3)
        }
    }
}
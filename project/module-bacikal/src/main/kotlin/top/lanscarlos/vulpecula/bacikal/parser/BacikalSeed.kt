package top.lanscarlos.vulpecula.bacikal.parser

import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.seed
 *
 * @author Lanscarlos
 * @since 2023-08-21 10:14
 */
interface BacikalSeed<T> {

    /**
     * 是否已接收 BacikalReader 并完成解析
     * */
    val isAccepted: Boolean

    fun accept(reader: BacikalReader)

    fun accept(frame: BacikalFrame): CompletableFuture<T>

}
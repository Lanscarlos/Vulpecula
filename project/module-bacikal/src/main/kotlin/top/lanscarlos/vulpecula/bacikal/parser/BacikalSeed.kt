package top.lanscarlos.vulpecula.bacikal.parser

import taboolib.module.kether.ScriptFrame
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.seed
 *
 * @author Lanscarlos
 * @since 2023-08-21 10:14
 */
interface BacikalSeed<T> {

    val isAccepted: Boolean

    fun accept(reader: BacikalReader)

    fun accept(frame: BacikalFrame): CompletableFuture<T>

}
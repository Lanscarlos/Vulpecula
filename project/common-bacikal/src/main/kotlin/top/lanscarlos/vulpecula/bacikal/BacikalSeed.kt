package top.lanscarlos.vulpecula.bacikal

import taboolib.module.kether.ScriptFrame
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal
 *
 * @author Lanscarlos
 * @since 2023-08-21 10:14
 */
interface BacikalSeed<T> {

    val isAccepted: Boolean

    fun accept(reader: BacikalReader)

    fun accept(frame: ScriptFrame): CompletableFuture<T>

}
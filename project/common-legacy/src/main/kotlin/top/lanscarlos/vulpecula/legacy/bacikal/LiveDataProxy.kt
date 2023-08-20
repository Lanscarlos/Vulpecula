package top.lanscarlos.vulpecula.legacy.bacikal

import taboolib.module.kether.ScriptFrame
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal
 *
 * @author Lanscarlos
 * @since 2023-02-27 13:45
 */
class LiveDataProxy<T>(
    vararg val prefix: String,
    val source: LiveData<T>,
    val def: T
) : LiveData<T>(source.func) {

    fun accept(prefix: String, reader: BacikalReader) {
        if (prefix !in this.prefix) return
        source.accept(reader)
    }

    override fun accept(reader: BacikalReader): LiveData<T> = source

    override fun accept(frame: ScriptFrame): CompletableFuture<T> {
        return if (source.isAccepted()) {
            source.accept(frame)
        } else {
            CompletableFuture.completedFuture(def)
        }
    }

}
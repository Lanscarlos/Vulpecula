package top.lanscarlos.vulpecula.kether.live

import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.run
import top.lanscarlos.vulpecula.utils.nextBlock
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.live
 *
 * @author Lanscarlos
 * @since 2022-11-10 16:40
 */
class StringListLiveData(
    val value: Any
) : LiveData<List<String>> {

    override fun get(frame: ScriptFrame, def: List<String>): CompletableFuture<List<String>> {
        return getOrNull(frame).thenApply { if (it != null) def else def }
    }

    override fun getOrNull(frame: ScriptFrame): CompletableFuture<List<String>?> {
        val future = if (value is ParsedAction<*>) {
            frame.run(value)
        } else CompletableFuture.completedFuture(value)

        return future.thenApply {
            when (it) {
                is String -> listOf(it)
                is Array<*> -> {
                    it.mapNotNull { it?.toString() }
                }
                is Collection<*> -> {
                    it.mapNotNull { it?.toString() }
                }
                else -> null
            }
        }
    }

    companion object {
        fun read(reader: QuestReader): LiveData<List<String>> {
            return StringListLiveData(reader.nextBlock())
        }
    }
}
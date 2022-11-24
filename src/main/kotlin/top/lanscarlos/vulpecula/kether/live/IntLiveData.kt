package top.lanscarlos.vulpecula.kether.live

import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.run
import top.lanscarlos.vulpecula.utils.nextBlock
import top.lanscarlos.vulpecula.utils.toInt
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.live
 *
 * @author Lanscarlos
 * @since 2022-11-10 15:32
 */
class IntLiveData(
    val value: Any
) : LiveData<Int> {

    override fun get(frame: ScriptFrame, def: Int): CompletableFuture<Int> {
        return getOrNull(frame).thenApply { if (it != null) def else def }
    }

    override fun getOrNull(frame: ScriptFrame): CompletableFuture<Int?> {
        val future = if (value is ParsedAction<*>) {
            frame.run(value)
        } else CompletableFuture.completedFuture(value)

        return future.thenApply {
            when (it) {
                "~" -> null
                is Short -> it.toInt()
                is Int -> it
                is Long -> it.toInt()
                is Float -> it.toInt()
                is Double -> it.toInt()
                is String -> it.toIntOrNull()
                else -> it?.toInt()
            }
        }
    }

    companion object {
        fun read(reader: QuestReader): LiveData<Int> {
            reader.mark()
            val value = reader.nextToken().toIntOrNull() ?: let {
                reader.reset()
                reader.nextBlock()
            }
            return IntLiveData(value)
        }
    }
}
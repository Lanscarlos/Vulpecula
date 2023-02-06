package top.lanscarlos.vulpecula.kether.live

import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.run
import top.lanscarlos.vulpecula.utils.nextBlock
import top.lanscarlos.vulpecula.utils.coerceDouble
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.live
 *
 * @author Lanscarlos
 * @since 2022-11-10 15:25
 */
class DoubleLiveData(
    val value: Any
) : LiveData<Double> {

    override fun get(frame: ScriptFrame, def: Double): CompletableFuture<Double> {
        return getOrNull(frame).thenApply { it ?: def }
    }

    override fun getOrNull(frame: ScriptFrame): CompletableFuture<Double?> {
        val future = if (value is ParsedAction<*>) {
            frame.run(value)
        } else CompletableFuture.completedFuture(value)

        return future.thenApply {
            when (it) {
                "~" -> null
                is Short -> it.toDouble()
                is Int -> it.toDouble()
                is Long -> it.toDouble()
                is Float -> it.toDouble()
                is Double -> it
                is String -> it.toDoubleOrNull()
                else -> it?.coerceDouble()
            }
        }
    }

    companion object {
        fun read(reader: QuestReader): LiveData<Double> {
            reader.mark()
            val value = reader.nextToken().toDoubleOrNull() ?: let {
                reader.reset()
                reader.nextBlock()
            }
            return DoubleLiveData(value)
        }
    }
}
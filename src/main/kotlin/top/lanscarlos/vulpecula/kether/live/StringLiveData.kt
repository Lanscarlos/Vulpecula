package top.lanscarlos.vulpecula.kether.live

import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.run
import top.lanscarlos.vulpecula.utils.hasNextToken
import top.lanscarlos.vulpecula.utils.nextBlock
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.live
 *
 * @author Lanscarlos
 * @since 2022-11-10 15:36
 */
class StringLiveData(
    val value: Any
) : LiveData<String> {

    override fun get(frame: ScriptFrame, def: String): CompletableFuture<String> {
        return getOrNull(frame).thenApply { it ?: def }
    }

    override fun getOrNull(frame: ScriptFrame): CompletableFuture<String?> {
        val future = if (value is ParsedAction<*>) {
            frame.run(value)
        } else CompletableFuture.completedFuture(value)

        return future.thenApply {
            when (it) {
                is String -> it
                else -> it?.toString()
            }
        }
    }

    companion object {

        /**
         *
         * ~ {token}
         * ~ parse &string
         *
         * */
        fun read(reader: QuestReader): LiveData<String> {
            val value: Any = if (reader.hasNextToken("parse")) {
                reader.nextBlock()
            } else {
                reader.nextToken()
            }
            return StringLiveData(value)
        }
    }
}
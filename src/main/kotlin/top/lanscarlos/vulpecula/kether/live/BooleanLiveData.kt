package top.lanscarlos.vulpecula.kether.live

import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.run
import top.lanscarlos.vulpecula.utils.nextBlock
import top.lanscarlos.vulpecula.utils.toBoolean

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.live
 *
 * @author Lanscarlos
 * @since 2022-11-12 14:15
 */
class BooleanLiveData(
    val value: Any
) : LiveData<Boolean> {

    override fun get(frame: ScriptFrame, def: Boolean): Boolean {
        val it = if (value is ParsedAction<*>) {
            frame.run(value).join()
        } else value

        return when (it) {
            "~" -> def
            is Boolean -> it
            "true", "yes" -> true
            "false", "no" -> false
            is Number -> it != 0
//            is String -> it.toDouble(def)
            else -> it.toBoolean(def)
        }
    }

    companion object {

        fun read(reader: QuestReader): LiveData<Boolean> {
            reader.mark()
            val value: Any = when (reader.nextToken()) {
                "true", "yes" -> true
                "false", "no" -> false
                else -> {
                    reader.reset()
                    reader.nextBlock()
                }
            }
            return BooleanLiveData(value)
        }
    }
}
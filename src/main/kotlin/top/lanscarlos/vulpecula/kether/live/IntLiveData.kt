package top.lanscarlos.vulpecula.kether.live

import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.run
import top.lanscarlos.vulpecula.utils.nextBlock
import top.lanscarlos.vulpecula.utils.toInt

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

    override fun get(frame: ScriptFrame, def: Int): Int {

        val it = if (value is ParsedAction<*>) {
            frame.run(value).join()
        } else value

        return when (it) {
            is Short -> it.toInt()
            is Int -> it
            is Long -> it.toInt()
            is Float -> it.toInt()
            is Double -> it.toInt()
//            is String -> it.toDouble(def)
            else -> it.toInt(def)
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
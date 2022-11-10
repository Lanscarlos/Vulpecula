package top.lanscarlos.vulpecula.kether.live

import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.run
import top.lanscarlos.vulpecula.utils.nextBlock

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

    override fun get(frame: ScriptFrame, def: List<String>): List<String> {
        val it = if (value is ParsedAction<*>) {
            frame.run(value).join()
        } else value

        return when (it) {
            is String -> listOf(it)
            is List<*> -> {
                it.mapNotNull { it?.toString() }
            }
            else -> def
        }
    }

    companion object {
        fun read(reader: QuestReader): LiveData<List<String>> {
            return StringListLiveData(reader.nextBlock())
        }
    }
}
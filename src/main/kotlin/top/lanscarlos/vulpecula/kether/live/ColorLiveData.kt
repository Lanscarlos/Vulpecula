package top.lanscarlos.vulpecula.kether.live

import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.expects
import top.lanscarlos.vulpecula.utils.*
import java.awt.Color

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.live
 *
 * @author Lanscarlos
 * @since 2022-11-10 15:51
 */
class ColorLiveData(
    val value: Any
) : LiveData<Color> {
    override fun get(frame: ScriptFrame, def: Color): Color {
        return when (value) {
            is Color -> value
            is Triple<*, *, *> -> {
                val r = (value.first as? IntLiveData)?.get(frame, def.red) ?: def.red
                val g = (value.second as? IntLiveData)?.get(frame, def.green) ?: def.green
                val b = (value.third as? IntLiveData)?.get(frame, def.blue) ?: def.blue
                Color(r, g, b)
            }
            is StringLiveData -> {
                val hex = value.get(frame, "FFFFFF")
                Color.decode(if (hex.startsWith("#")) hex.substring(1) else hex)
            }
            else -> def
        }
    }

    companion object {

        /**
         *
         * rgb r g b
         * rgb &r &g &b
         *
         * hex #aaffcc
         * hex &hex
         *
         * */
        fun read(reader: QuestReader): LiveData<Color> {
            val value: Any = when (reader.expects("rgb", "hex")) {
                "rgb" -> {
                    val r = reader.readInt()
                    val g = reader.readInt()
                    val b = reader.readInt()
                    Triple(r, g, b)
                }
                "hex" -> {
                    if (reader.nextPeek().startsWith('#')) {
                        val hex = reader.nextToken().substring(1)
                        Color.decode(hex)
                    } else {
                        reader.readString()
                    }
                }
                else -> Color.WHITE
            }
            return ColorLiveData(value)
        }
    }
}
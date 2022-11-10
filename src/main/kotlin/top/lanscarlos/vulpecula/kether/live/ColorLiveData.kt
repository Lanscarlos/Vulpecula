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
                val red = (value.first as? IntLiveData)?.get(frame, def.red) ?: def.red
                val green = (value.second as? IntLiveData)?.get(frame, def.green) ?: def.green
                val blue = (value.third as? IntLiveData)?.get(frame, def.blue) ?: def.blue
                Color(red, green, blue)
            }
            is Pair<*, *> -> {
                val base = value.first as? Triple<*, *, *> ?: return def
                val red = (base.first as? IntLiveData)?.get(frame, def.red) ?: def.red
                val green = (base.second as? IntLiveData)?.get(frame, def.green) ?: def.green
                val blue = (base.third as? IntLiveData)?.get(frame, def.blue) ?: def.blue
                val alpha = (value.second as? IntLiveData)?.get(frame, def.alpha) ?: def.alpha
                Color(red, green, blue, alpha)
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
         * rgba r g b a
         * rgba &r &g &b &a
         *
         * hex #aaffcc
         * hex &hex
         *
         * */
        fun read(reader: QuestReader): LiveData<Color> {
            val value: Any = when (reader.expects("rgb", "rgba", "hex")) {
                "rgb" -> {
                    val red = reader.readInt()
                    val green = reader.readInt()
                    val blue = reader.readInt()
                    Triple(red, green, blue)
                }
                "rgba" -> {
                    val red = reader.readInt()
                    val green = reader.readInt()
                    val blue = reader.readInt()
                    val alpha = reader.readInt()
                    Triple(red, green, blue) to alpha
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
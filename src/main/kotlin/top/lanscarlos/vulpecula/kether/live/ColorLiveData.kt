package top.lanscarlos.vulpecula.kether.live

import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.expects
import taboolib.module.kether.run
import top.lanscarlos.vulpecula.utils.*
import java.awt.Color
import java.util.concurrent.CompletableFuture

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

    override fun get(frame: ScriptFrame, def: Color): CompletableFuture<Color> {
        return when (value) {
            is Color -> CompletableFuture.completedFuture(value)
            is ParsedAction<*> -> {
                frame.run(value).thenApply { it as? Color ?: def }
            }
            is Triple<*, *, *> -> {
                listOf(
                    (value.first as? IntLiveData)?.getOrNull(frame),
                    (value.second as? IntLiveData)?.getOrNull(frame),
                    (value.third as? IntLiveData)?.getOrNull(frame)
                ).thenTake().thenApply {
                    Color(it[0].toInt(def.red), it[1].toInt(def.green), it[2].toInt(def.blue))
                }
            }
            is Pair<*, *> -> {
                val base = value.first as? Triple<*, *, *> ?: return CompletableFuture.completedFuture(def)
                listOf(
                    (base.first as? IntLiveData)?.getOrNull(frame),
                    (base.second as? IntLiveData)?.getOrNull(frame),
                    (base.third as? IntLiveData)?.getOrNull(frame),
                    (value.second as? IntLiveData)?.getOrNull(frame)
                ).thenTake().thenApply {
                    Color(it[0].toInt(def.red), it[1].toInt(def.green), it[2].toInt(def.blue), it[3].toInt(def.alpha))
                }
            }
            is StringLiveData -> {
                value.getOrNull(frame).thenApply {
                    val hex = it ?: return@thenApply def
                    Color.decode(if (hex.startsWith("#")) hex.substring(1) else hex)
                }
            }
            else -> CompletableFuture.completedFuture(def)
        }
    }

    override fun getOrNull(frame: ScriptFrame): CompletableFuture<Color?> {
        return when (value) {
            is Color -> CompletableFuture.completedFuture(value)
            is ParsedAction<*> -> {
                frame.run(value).thenApply { it as? Color }
            }
            is Triple<*, *, *> -> {
                listOf(
                    (value.first as? IntLiveData)?.getOrNull(frame),
                    (value.second as? IntLiveData)?.getOrNull(frame),
                    (value.third as? IntLiveData)?.getOrNull(frame)
                ).thenTake().thenApply {
                    Color(
                        it[0]?.toInt() ?: return@thenApply null,
                        it[1]?.toInt() ?: return@thenApply null,
                        it[2]?.toInt() ?: return@thenApply null
                    )
                }
            }
            is Pair<*, *> -> {
                val base = value.first as? Triple<*, *, *> ?: return CompletableFuture.completedFuture(null)
                listOf(
                    (base.first as? IntLiveData)?.getOrNull(frame),
                    (base.second as? IntLiveData)?.getOrNull(frame),
                    (base.third as? IntLiveData)?.getOrNull(frame),
                    (value.second as? IntLiveData)?.getOrNull(frame)
                ).thenTake().thenApply {
                    Color(
                        it[0]?.toInt() ?: return@thenApply null,
                        it[1]?.toInt() ?: return@thenApply null,
                        it[2]?.toInt() ?: return@thenApply null,
                        it[3]?.toInt() ?: return@thenApply null
                    )
                }
            }
            is StringLiveData -> {
                value.getOrNull(frame).thenApply {
                    val hex = it ?: return@thenApply null
                    Color.decode(if (hex.startsWith("#")) hex.substring(1) else hex)
                }
            }
            else -> CompletableFuture.completedFuture(null)
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
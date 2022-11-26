package top.lanscarlos.vulpecula.kether.action.canvas

import taboolib.common.platform.ProxyParticle
import taboolib.common.util.Vector
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.scriptParser
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.utils.*
import java.awt.Color
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.canvas
 *
 * @author Lanscarlos
 * @since 2022-11-08 23:25
 */

class ActionBrush(val options: Map<String, LiveData<*>>) : ScriptAction<CanvasBrush>() {

    override fun run(frame: ScriptFrame): CompletableFuture<CanvasBrush> {
        val brush = frame.getVariable<CanvasBrush>(ActionCanvas.VARIABLE_BRUSH) ?: CanvasBrush().also {
            frame.setVariable(ActionCanvas.VARIABLE_BRUSH, it)
        }
        return options.mapValues { it.value.getOrNull(frame) }.thenTake().thenApply { args ->
            for (it in args) {
                modify(brush, frame, it.key, it.value)
            }

            return@thenApply brush
        }
    }

    companion object {

        @VulKetherParser(
            id = "brush",
            name = ["brush", "pen"],
            namespace = "vulpecula-canvas"
        )
        fun parser() = scriptParser { reader ->
            val options = mutableMapOf<String, LiveData<*>>()
            while (reader.nextPeek().startsWith('-')) {
                read(reader, reader.nextToken().substring(1), options)
            }
            ActionBrush(options)
        }

        /**
         * 读取与粒子笔刷相关的参数
         *
         * @param option 参数名
         * @param options 参数集合缓存
         * */
        fun read(reader: QuestReader, option: String, options: MutableMap<String, LiveData<*>>) {
            when (option) {
                "type", "t" -> {
                    options["type"] = reader.readString()
                }
                "count", "c" -> {
                    options["count"] = reader.readInt()
                }
                "speed", "sp" -> {
                    options["speed"] = reader.readDouble()
                }
                "offset", "o" -> {
                    options["offset"] = reader.readVector(!reader.hasNextToken("to"))
                }
                "spread", "s" -> {
                    options["spread"] = reader.readVector(!reader.hasNextToken("to"))
                }
                "velocity", "vel", "v" -> {
                    options["velocity"] = reader.readVector(!reader.hasNextToken("to"))
                }

                "size" -> {
                    options["size"] = reader.readInt()
                }
                "color" -> {
                    options["color"] = reader.readColor()
                    if (reader.hasNextToken("to")) {
                        options["transition"] = reader.readColor()
                    }
                }
                "transition" -> {
                    options["transition"] = reader.readColor()
                }
                "material", "mat" -> {
                    options["material"] = reader.readString()
                    if (reader.hasNextToken("with")) {
                        options["data"] = reader.readInt()
                    }
                }
                "data" -> {
                    options["data"] = reader.readInt()
                }
                "name" -> {
                    options["name"] = reader.readString()
                }
                "lore" -> {
                    options["lore"] = reader.readStringList()
                }
                "customModelData", "model" -> {
                    options["model"] = reader.readInt()
                }
            }
        }

        /**
         * 修改笔刷属性
         *
         * @param option 属性名
         * @param value 属性值
         * */
        fun modify(brush: CanvasBrush, frame: ScriptFrame, option: String, value: Any?) {
            when (option) {
                "type" -> {
                    val type = value?.toString()?.uppercase() ?: return
                    brush.particle = ProxyParticle.values().firstOrNull { it.name.equals(type, true) } ?: return
                }
                "count" -> brush.count = value?.toInt() ?: return
                "speed" -> brush.speed = value?.toDouble() ?: return
                "offset" -> brush.offset = value as? Vector ?: return
                "spread" -> brush.vector = value as? Vector ?: return
                "velocity" -> {
                    brush.vector = value as? Vector ?: return
                    brush.count = 0
                    if (brush.speed == 0.0) brush.speed = 0.15
                }

                "size" -> brush.size = value?.toFloat() ?: return
                "color" -> {
                    val color = value as? Color ?: return
                    when (brush.particle) {
                        ProxyParticle.SPELL_MOB,
                        ProxyParticle.SPELL_MOB_AMBIENT -> {
                            brush.color = color
                            brush.count = 0
                            brush.speed = color.alpha.div(255.0)
                            brush.vector.x = color.red.div(255.0)
                            brush.vector.y = color.green.div(255.0)
                            brush.vector.z = color.blue.div(255.0)
                        }
                        else -> {
                            brush.color = color
                        }
                    }
                }
                "transition" -> brush.transition = value as? Color ?: return
                "material" -> brush.material = value?.toString() ?: return
                "data" -> brush.data = value?.toInt() ?: return
                "name" -> brush.name = value?.toString() ?: return
                "lore" -> brush.lore = value as? List<String> ?: return
                "model" -> brush.customModelData = value?.toInt() ?: return
            }
        }
    }
}
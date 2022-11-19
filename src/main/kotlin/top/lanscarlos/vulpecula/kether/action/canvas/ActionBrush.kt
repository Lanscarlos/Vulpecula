package top.lanscarlos.vulpecula.kether.action.canvas

import taboolib.common.platform.ProxyParticle
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.scriptParser
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.kether.live.DoubleLiveData
import top.lanscarlos.vulpecula.kether.live.IntLiveData
import top.lanscarlos.vulpecula.utils.*
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
        for (it in options) {
            modify(brush, frame, it.key, it.value)
        }
        return CompletableFuture.completedFuture(brush)
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
                    options["vector"] = reader.readVector(!reader.hasNextToken("to"))
                }
                "velocity", "vel", "v" -> {
                    options["vector"] = reader.readVector(!reader.hasNextToken("to"))
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
        fun modify(brush: CanvasBrush, frame: ScriptFrame, option: String, value: LiveData<*>) {
            when (option) {
                "type" -> {
                    brush.particle = ProxyParticle.valueOf(value.getValue(frame, brush.particle.name).uppercase())
                }
                "count" -> brush.count = value.getValue(frame, brush.count)
                "speed" -> brush.speed = value.getValue(frame, brush.speed)
                "offset" -> brush.offset = value.getValue(frame, brush.offset)
                "vector" -> {
                    brush.count = 0
                    brush.speed = if (brush.speed != 0.0) brush.speed else 0.15
                    brush.vector = value.getValue(frame, brush.vector)
                }

                "size" -> brush.size = value.getValue(frame, brush.size.toDouble()).toFloat()
                "color" -> {
                    val color = value.getValue(frame, brush.color)
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
                "transition" -> brush.transition = value.getValue(frame, brush.transition)
                "material" -> brush.material = value.getValue(frame, brush.material)
                "data" -> brush.data = value.getValue(frame, brush.data)
                "name" -> brush.name = value.getValue(frame, brush.name)
                "lore" -> brush.lore = value.getValue(frame, brush.lore)
                "model" -> brush.customModelData = value.getValue(frame, brush.customModelData)
            }
        }
    }
}
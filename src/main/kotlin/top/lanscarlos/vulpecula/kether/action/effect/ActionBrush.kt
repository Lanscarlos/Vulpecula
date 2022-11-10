package top.lanscarlos.vulpecula.kether.action.effect

import taboolib.common.platform.ProxyParticle
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.run
import taboolib.module.kether.scriptParser
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.utils.*
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.effect
 *
 * @author Lanscarlos
 * @since 2022-11-08 23:25
 */

class ActionBrush(val options: Map<String, Any>) : ScriptAction<CanvasBrush>() {

    override fun run(frame: ScriptFrame): CompletableFuture<CanvasBrush> {
        val brush = frame.getVariable<CanvasBrush>("@Brush") ?: CanvasBrush().also {
            frame.setVariable("@Brush", it)
        }
        val iterator = options.iterator()
        while (iterator.hasNext()) {
            val it = iterator.next()
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
            val options = mutableMapOf<String, Any>()
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
        fun read(reader: QuestReader, option: String, options: MutableMap<String, Any>) {
            when (option) {
                "type", "t" -> {
                    options["type"] = if (reader.hasNextToken("to")) {
                        reader.nextBlock()
                    } else {
                        ProxyParticle.valueOf(reader.nextToken().uppercase())
                    }
                }
                "count", "c" -> {
                    options["count"] = reader.readInt("to")
                }
                "speed", "sp" -> {
                    options["speed"] = reader.readDouble("to")
                }
                "offset", "o" -> {
                    options["offset"] = reader.readVector()
                }
                "spread", "s" -> {
                    options["vector"] = reader.readVector()
                }
                "velocity", "vel", "v" -> {
                    options["vector"] = reader.readVector()
                    options["count"] = 0
                    options["speed"] = options["speed"].toDouble(0.0).coerceAtLeast(0.15)
                }

                "size" -> {
                    options["size"] = reader.readInt("to")
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
                    options["material"] = reader.readString("to")
                    if (reader.hasNextToken("with")) {
                        options["data"] = reader.readInt("to")
                    }
                }
                "data" -> {
                    options["data"] = reader.readInt("to")
                }
                "name" -> {
                    options["name"] = reader.readString("to")
                }
                "lore" -> {
                    options["lore"] = reader.nextBlock()
                }
                "customModelData" -> {
                    options["data"] = reader.readInt("to")
                }
            }
        }

        /**
         * 修改笔刷属性
         *
         * @param option 属性名
         * @param value 属性值
         * */
        fun modify(brush: CanvasBrush, frame: ScriptFrame, option: String, value: Any) {
            when (option) {
                "type" -> brush.particle = frame.coerceParticle(value, brush.particle)
                "count" -> brush.count = frame.coerceInt(value, brush.count)
                "speed" -> brush.speed = frame.coerceDouble(value, brush.speed)
                "offset" -> brush.offset = frame.coerceVector(value, brush.offset)
                "vector" -> brush.vector = frame.coerceVector(value, brush.vector)

                "size" -> brush.size = frame.coerceDouble(value, brush.size.toDouble()).toFloat()
                "color" -> brush.color = frame.coerceColor(value, brush.color)
                "transition" -> brush.transition = frame.coerceColor(value, brush.transition)
                "material" -> brush.material = frame.coerceString(value, brush.material)
                "data" -> brush.data = frame.coerceInt(value, brush.data)
                "name" -> brush.name = frame.coerceString(value, brush.name)
                "lore" -> {
                    brush.lore = (frame.run(value as ParsedAction<*>).join() as? List<*>)?.mapNotNull { it?.toString() } ?: brush.lore
                }
                "customModelData" -> brush.customModelData = frame.coerceInt(value, brush.customModelData)
            }
        }
    }
}
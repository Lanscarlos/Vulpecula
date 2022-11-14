package top.lanscarlos.vulpecula.kether.action.canvas

import taboolib.common.util.Location
import taboolib.module.kether.*
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.kether.action.canvas.pattern.CanvasPattern
import top.lanscarlos.vulpecula.utils.*
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.canvas
 *
 * @author Lanscarlos
 * @since 2022-11-10 11:14
 */
class ActionPattern(val builder: CanvasPattern.Builder) : ScriptAction<Any>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Any> {
        val pattern = builder.build(frame)
        frame.setVariable(ActionCanvas.VARIABLE_PATTERN, pattern)
        return CompletableFuture.completedFuture(pattern)
    }

    companion object {

        /**
         *
         * 获取图案的下一点坐标
         * pattern next [origin &origin] [by &pattern]
         *
         * 获取图案的所有点坐标
         * pattern points [origin &origin] [by &pattern]
         *
         * 定义图案
         * pattern [other token]
         * pattern line [from xxx] to xxx
         * pattern
         *
         * */
        @VulKetherParser(
            id = "pattern",
            name = ["pattern"],
            namespace = "vulpecula-canvas"
        )
        fun parser() = scriptParser { reader ->
            when (val name = reader.nextToken()) {
                "next" -> {
                    val originRaw = if (reader.hasNextToken("origin")) {
                        reader.readLocation()
                    } else {
                        null
                    }
                    val patternRaw = if (reader.hasNextToken("by")) {
                        reader.nextBlock()
                    } else {
                        null
                    }
                    actionFuture { future ->
                        val pattern = patternRaw?.let {
                            this.run(it).join() as? CanvasPattern
                        } ?: this.getVariable<CanvasPattern>(ActionCanvas.VARIABLE_PATTERN) ?: error("No canvas pattern selected.")

                        val base = this.getVariable<Location>(ActionCanvas.VARIABLE_ORIGIN) ?: player().location
                        val origin = originRaw?.get(frame = this, base) ?: base
                        future.complete(pattern.nextPoint(origin))
                    }
                }
                "points" -> {
                    val originRaw = if (reader.hasNextToken("origin")) {
                        reader.readLocation()
                    } else {
                        null
                    }
                    val patternRaw = if (reader.hasNextToken("by")) {
                        reader.nextBlock()
                    } else {
                        null
                    }
                    actionFuture { future ->
                        val pattern = patternRaw?.let {
                            this.run(it).join() as? CanvasPattern
                        } ?: this.getVariable<CanvasPattern>(ActionCanvas.VARIABLE_PATTERN) ?: error("No canvas pattern selected.")

                        val base = this.getVariable<Location>(ActionCanvas.VARIABLE_ORIGIN) ?: player().location
                        val origin = originRaw?.get(frame = this, base) ?: base
                        future.complete(pattern.points(origin))
                    }
                }
                else -> {
                    val patternReader = CanvasPattern.getReader(name) ?: error("Unknown pattern type: \"$name\"")
                    ActionPattern(patternReader.read(reader))
                }
            }
        }
    }
}
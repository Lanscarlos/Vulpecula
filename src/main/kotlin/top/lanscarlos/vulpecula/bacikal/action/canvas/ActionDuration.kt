package top.lanscarlos.vulpecula.bacikal.action.canvas

import taboolib.module.kether.*
import top.lanscarlos.vulpecula.bacikal.BacikalParser
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.utils.getVariable
import top.lanscarlos.vulpecula.utils.setVariable
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.canvas
 *
 * @author Lanscarlos
 * @since 2022-11-09 22:22
 */
class ActionDuration(val duration: Int, val between: Boolean) : ScriptAction<Boolean>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Boolean> {
        val startTime = frame.getVariable<Long>(ActionCanvas.VARIABLE_DURATION_START) ?: error("No canvas start time record.")
        val endTime = frame.getVariable<Long>(ActionCanvas.VARIABLE_DURATION_END) ?: (startTime + duration * 50L).also {
            frame.setVariable(ActionCanvas.VARIABLE_DURATION_END, it)
        }
        return CompletableFuture.completedFuture(if (between) System.currentTimeMillis() <= endTime else System.currentTimeMillis() > endTime)
    }

    companion object {

        /**
         * 判断画布工作时间是否处于指定 ticks 内
         * duration in {ticks}
         * duration in 200
         *
         * duration out 200
         * */
        @BacikalParser(
            id = "duration",
            name = ["duration"],
            namespace = "vulpecula-canvas"
        )
        fun parser() = scriptParser { reader ->
            reader.switch {
                case("in") {
                    ActionDuration(reader.nextInt(), true)
                }
                case("out") {
                    ActionDuration(reader.nextInt(), false)
                }
            }
        }
    }
}
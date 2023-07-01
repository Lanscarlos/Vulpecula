package top.lanscarlos.vulpecula.bacikal.action.canvas

import taboolib.module.kether.*
import top.lanscarlos.vulpecula.bacikal.BacikalParser
import top.lanscarlos.vulpecula.bacikal.bacikalSwitch
import top.lanscarlos.vulpecula.utils.getVariable
import top.lanscarlos.vulpecula.utils.setVariable

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.canvas
 *
 * @author Lanscarlos
 * @since 2022-11-09 22:22
 */
object ActionDuration {

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
    fun parser() = bacikalSwitch {
        case("in") {
            combine(
                int()
            ) { duration ->
                check(this, duration, true)
            }
        }
        case("out") {
            combine(
                int()
            ) { duration ->
                check(this, duration, false)
            }
        }
        case("show") {
            discrete {
                val startTime = this.getVariable<Long>(ActionCanvas.VARIABLE_DURATION_START)
                    ?: error("No canvas start time record.")
                return@discrete (System.currentTimeMillis() - startTime) / 50
            }
        }
    }

    fun check(frame: ScriptFrame, duration: Int, between: Boolean): Boolean {
        val startTime =
            frame.getVariable<Long>(ActionCanvas.VARIABLE_DURATION_START) ?: error("No canvas start time record.")
        val endTime = frame.getVariable<Long>(ActionCanvas.VARIABLE_DURATION_END) ?: (startTime + duration * 50L).also {
            frame.setVariable(ActionCanvas.VARIABLE_DURATION_END, it)
        }
        return if (between) System.currentTimeMillis() <= endTime else System.currentTimeMillis() > endTime
    }
}
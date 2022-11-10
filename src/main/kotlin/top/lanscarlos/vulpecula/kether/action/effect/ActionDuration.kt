package top.lanscarlos.vulpecula.kether.action.effect

import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.scriptParser
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.kether.action.ActionDuration
import top.lanscarlos.vulpecula.utils.getVariable
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.effect
 *
 * @author Lanscarlos
 * @since 2022-11-09 22:22
 */
class ActionDuration : ScriptAction<Int>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Int> {
        val startTime = frame.getVariable<Long>("@StartTime") ?: error("No start time record.")
        val duration = (System.currentTimeMillis() - startTime) / 50L
        return CompletableFuture.completedFuture(duration.toInt())
    }

    companion object {
        @VulKetherParser(
            id = "duration",
            name = ["duration"],
            namespace = "vulpecula-canvas"
        )
        fun parser() = scriptParser {
            ActionDuration()
        }
    }
}
package top.lanscarlos.vulpecula.kether.action

import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.scriptParser
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.utils.getVariable
import top.lanscarlos.vulpecula.utils.toLong
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action
 *
 * @author Lanscarlos
 * @since 2022-11-08 17:48
 */

class ActionDuration : ScriptAction<Long>() {
    override fun run(frame: ScriptFrame): CompletableFuture<Long> {
        val startTime = frame.getVariable<Any>("@StartTime")?.toLong() ?: error("No start time record.")
        val duration = System.currentTimeMillis() - startTime
        return CompletableFuture.completedFuture(duration / 50L)
    }

    companion object {
        @VulKetherParser(
            id = "duration",
            name = ["duration"],
            override = ["duration"]
        )
        fun parser() = scriptParser {
            ActionDuration()
        }
    }
}
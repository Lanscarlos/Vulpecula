package top.lanscarlos.vulpecula.bacikal.action.internal

import top.lanscarlos.vulpecula.bacikal.BacikalParser
import top.lanscarlos.vulpecula.bacikal.bacikal

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.internal
 *
 * @author Lanscarlos
 * @since 2023-03-25 00:29
 */
object ActionVulpecula {

    @BacikalParser(
        id = "vulpecula",
        name = ["vulpecula", "vul"]
    )
    fun parser() = bacikal {
        when (val next = this.nextToken()) {
            "dispatcher" -> ActionVulpeculaDispatcher.resolve(this)
            "schedule" -> ActionVulpeculaSchedule.resolve(this)
            "script" -> ActionVulpeculaScript.resolve(this)
            else -> error("Unknown sub action \"$next\" at vulpecula action.")
        }
    }
}
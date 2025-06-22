package top.lanscarlos.vulpecula.legacy.bacikal.action.internal

import top.lanscarlos.vulpecula.legacy.bacikal.Bacikal
import top.lanscarlos.vulpecula.legacy.bacikal.BacikalParser
import top.lanscarlos.vulpecula.legacy.bacikal.BacikalReader
import top.lanscarlos.vulpecula.legacy.bacikal.bacikal
import top.lanscarlos.vulpecula.legacy.internal.ScheduleTask

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.internal
 *
 * @author Lanscarlos
 * @since 2023-03-25 23:25
 */
object ActionVulpeculaSchedule {

    @BacikalParser(
        id = "vulpecula-schedule",
        aliases = ["vul-schedule"]
    )
    fun parser() = bacikal {
        resolve(this)
    }

    fun resolve(reader: BacikalReader): Bacikal.Parser<Any?> {
        return reader.run {
            this.mark()
            when (this.nextToken()) {
                "run" -> {
                    combine(
                        text(display = "schedule id"),
                        optional("with", then = list())
                    ) { id, args ->
                        if (args != null) {
                            ScheduleTask.get(id)?.runTask(args.toTypedArray())
                        } else {
                            ScheduleTask.get(id)?.runTask()
                        } ?: error("No schedule found: \"$id\"")
                    }
                }
                "stop" -> {
                    combine(
                        text(display = "schedule id")
                    ) { id ->
                        try {
                            if (id == "*") {
                                ScheduleTask.cache.values.forEach { it.terminate() }
                            } else {
                                ScheduleTask.get(id)?.terminate()
                            }
                            return@combine true
                        } catch (e: Exception) {
                            e.printStackTrace()
                            return@combine false
                        }
                    }
                }
                else -> {
                    this.reset()
                    discrete {
                        ScheduleTask.cache.keys
                    }
                }
            }
        }
    }

}
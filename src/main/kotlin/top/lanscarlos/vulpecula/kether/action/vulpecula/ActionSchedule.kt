package top.lanscarlos.vulpecula.kether.action.vulpecula

import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.*
import top.lanscarlos.vulpecula.internal.ScheduleTask
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.kether.live.readString
import top.lanscarlos.vulpecula.utils.hasNextToken
import top.lanscarlos.vulpecula.utils.thenTake
import top.lanscarlos.vulpecula.utils.tryNextActionList
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.vulpecula
 *
 * @author Lanscarlos
 * @since 2023-02-06 20:15
 */
object ActionSchedule {

    class ActionScheduleRun(
        val taskId: LiveData<String>,
        val args: List<ParsedAction<*>>?
    ) : ScriptAction<Any?>() {
        override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
            return listOf(
                taskId.getOrNull(frame),
                *(args ?: emptyList()).map { frame.run(it) }.toTypedArray()
            ).thenTake().thenApply { args ->
                val taskId = args[0]?.toString() ?: error("No schedule id found: \"${args[0]}\"")
                val task = ScheduleTask.get(taskId) ?: error("No schedule found: \"${args[0]}\"")

                if (args.size > 1) {
                    // 有参数
                    task.runTask(*args.subList(1, args.size).toTypedArray())
                } else {
                    // 无参数
                    task.runTask()
                }
            }
        }
    }

    fun parse(reader: QuestReader): ScriptAction<*> {
        return reader.switch {
            case("run") {
                val taskId = reader.readString()
                val args = reader.tryNextActionList("with")

                ActionScheduleRun(taskId, args)
            }

            case("stop") {
                if (reader.hasNextToken("*")) {
                    actionNow {
                        try {
                            ScheduleTask.cache.values.forEach { it.terminate() }
                            return@actionNow true
                        } catch (e: Exception) {
                            e.printStackTrace()
                            return@actionNow false
                        }
                    }
                } else {
                    val taskId = reader.readString()
                    actionTake {
                        taskId.get(this, "null").thenApply {
                            val task = ScheduleTask.get(it) ?: error("No schedule id found: \"$it\"")
                            try {
                                task.terminate()
                                return@thenApply true
                            } catch (e: Exception) {
                                e.printStackTrace()
                                return@thenApply false
                            }
                        }
                    }
                }
            }
        }
    }

    @VulKetherParser(
        id = "vulpecula-schedule",
        name = ["vul-schedule", "vul-task"]
    )
    fun parser() = scriptParser { reader ->
        parse(reader)
    }

}
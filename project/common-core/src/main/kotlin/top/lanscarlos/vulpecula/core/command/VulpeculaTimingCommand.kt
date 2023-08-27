package top.lanscarlos.vulpecula.core.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.function.submit
import taboolib.common5.format
import taboolib.module.kether.printKetherErrorMessage
import taboolib.platform.type.BukkitPlayer
import top.lanscarlos.vulpecula.applicative.PrimitiveApplicative.applicativeInt
import top.lanscarlos.vulpecula.bacikal.toBacikalQuest
import top.lanscarlos.vulpecula.config.bindConfigSection
import top.lanscarlos.vulpecula.core.utils.timing
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.core.command
 *
 * @author Lanscarlos
 * @since 2023-08-27 11:46
 */
object VulpeculaTimingCommand {

    val repeat: Int by bindConfigSection("command-timing-repeat") {
        it?.applicativeInt()?.getValue() ?: 10000
    }

    val executor: CommandComponent.() -> Unit = {
        dynamic {
            execute<ProxyCommandSender> { sender, _, content ->
                submit(async = true) {
                    try {
                        val quest = content.toBacikalQuest("vulpecula-eval")
                        val start = timing()
                        val memory = timingMemory()
                        val futures = Array<CompletableFuture<*>?>(repeat) { null }
                        repeat(repeat) { index ->
                            futures[index] = quest.runActions {
                                this.sender = sender
                                if (sender is BukkitPlayer) {
                                    setVariable("player", sender.player)
                                    setVariable("hand", sender.player.equipment?.itemInMainHand)
                                }
                            }
                        }

                        val delayRecord = timing(start)
                        val memoryRecord = timingMemory(memory)
                        for (it in futures) {
                            it?.join()
                        }
                        val completedRecord = timing(start)
                        sender.sendMessage(" §5§l‹ ›§r §7启动耗时: §c${delayRecord.format(3)}ms§7; 内存占用: §c${memoryRecord.format(3)}MB§7; 完成耗时: §c${completedRecord.format(3)}ms")
                    } catch (e: Exception) {
                        e.printKetherErrorMessage()
                    }
                }
            }
        }
    }

    private fun timingMemory(): Long {
        val runtime = Runtime.getRuntime()
        return runtime.totalMemory() - runtime.freeMemory()
    }

    private fun timingMemory(start: Long): Double {
        return (timingMemory() - start) / 1048576.0
    }
}
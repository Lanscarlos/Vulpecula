package top.lanscarlos.vulpecula.bacikal.command

import org.bukkit.entity.Player
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.submit
import taboolib.common5.format
import taboolib.module.kether.printKetherErrorMessage
import top.lanscarlos.vulpecula.applicative.applicativeInt
import top.lanscarlos.vulpecula.bacikal.toBacikalQuest
import top.lanscarlos.vulpecula.config.bindConfigSection
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.command
 *
 * @author Lanscarlos
 * @since 2024-05-15 10:21
 */
object TimingCommand {

    /**
     * 重复次数
     * */
    private val repeat: Int by bindConfigSection("command-timing-repeat") {
        it?.applicativeInt() ?: 10000
    }

    @CommandBody
    val timing = subCommand {
        dynamic {
            execute<ProxyCommandSender> { sender, _, content ->
                submit(async = true) {
                    try {
                        val quest = content.toBacikalQuest("vulpecula-eval")
                        val start = top.lanscarlos.vulpecula.utils.timing()
                        val memory = timingMemory()
                        val futures = Array<CompletableFuture<*>?>(repeat) { null }
                        repeat(repeat) { index ->
                            futures[index] = quest.runActions {
                                this.sender = sender
                                sender.castSafely<Player>()?.let { player ->
                                    setVariable("player", player)
                                    setVariable("hand", player.equipment?.itemInMainHand)
                                }
                            }
                        }

                        val delayRecord = top.lanscarlos.vulpecula.utils.timing(start)
                        val memoryRecord = timingMemory(memory)
                        for (it in futures) {
                            it?.join()
                        }
                        val completedRecord = top.lanscarlos.vulpecula.utils.timing(start)
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
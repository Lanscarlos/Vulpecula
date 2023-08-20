package top.lanscarlos.vulpecula.legacy.command

import org.bukkit.command.CommandSender
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.function.adaptCommandSender
import taboolib.common5.format
import taboolib.module.kether.KetherShell
import taboolib.module.kether.ScriptOptions
import top.lanscarlos.vulpecula.legacy.utils.timing

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.command
 *
 * @author Lanscarlos
 * @since 2023-04-27 09:29
 */
object CommandUtilTiming {
    fun main(component: CommandComponent) {
        component.literal("timing", permission = "vulpecula.command.util.timing") {
            dynamic {
                execute<CommandSender> { sender, _, argument ->
                    val cache = mutableListOf<Double>()
                    repeat(10) {
                        val start = timing()
                        repeat(100000) {
                            KetherShell.eval(
                                argument,
                                ScriptOptions(
                                    sender = adaptCommandSender(sender)
                                )
                            ).getNow(null)
                        }
                        cache += timing(start)
                    }
                    sender.sendMessage(" §5§l‹ ›§r §7平均耗时: §f${cache.average().format(3)}ms")
                }
            }
        }
    }
}
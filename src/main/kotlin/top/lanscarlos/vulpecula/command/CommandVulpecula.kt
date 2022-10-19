package top.lanscarlos.vulpecula.command

import org.bukkit.entity.Player
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.info
import taboolib.module.kether.Kether
import top.lanscarlos.vulpecula.utils.eval

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.command
 *
 * @author Lanscarlos
 * @since 2022-10-17 20:39
 */
@CommandHeader(name = "vulpecula", aliases = ["vul"])
object CommandVulpecula {

    @CommandBody
    val eval = subCommand {
        dynamic {
            execute<Player> { player, _, argument ->
                val result = eval(argument, player, args = mapOf("player" to player)).get()
                player.sendMessage("运行结果：$result")
            }
        }
    }

}
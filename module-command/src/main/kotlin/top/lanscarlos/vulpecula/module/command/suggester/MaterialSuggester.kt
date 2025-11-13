package top.lanscarlos.vulpecula.module.command.suggester

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandContext
import taboolib.library.xseries.XMaterial
import top.lanscarlos.vulpecula.module.command.Suggester

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.command
 *
 * @author Lanscarlos
 * @since 2025/4/29 15:06
 */
object MaterialSuggester : Suggester {

    override fun suggest(sender: ProxyCommandSender, context: CommandContext<ProxyCommandSender>): List<String> {
        return XMaterial.entries.map { it.name }
    }

}
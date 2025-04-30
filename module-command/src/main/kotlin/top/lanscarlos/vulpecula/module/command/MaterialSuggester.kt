package top.lanscarlos.vulpecula.module.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandContext
import taboolib.library.xseries.XMaterial

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.command
 *
 * @author Lanscarlos
 * @since 2025/4/29 15:06
 */
object MaterialSuggester : Suggester<XMaterial> {

    override fun <T : ProxyCommandSender> suggest(sender: T, context: CommandContext<T>): List<String> {
        return XMaterial.entries.map { it.name }
    }

    override fun convert(input: String): XMaterial {
        return XMaterial.entries.find { it.name == input } ?: error("Invalid input.")
    }

}
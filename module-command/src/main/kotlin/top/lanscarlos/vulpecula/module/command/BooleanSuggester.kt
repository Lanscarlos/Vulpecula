package top.lanscarlos.vulpecula.module.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandContext
import top.lanscarlos.vulpecula.common.applicative.BooleanApplicative

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.command
 *
 * @author Lanscarlos
 * @since 2025/4/29 14:03
 */
object BooleanSuggester : Suggester<Boolean> {

    override fun <T : ProxyCommandSender> suggest(sender: T, context: CommandContext<T>): List<String> {
        return listOf("true", "false")
    }

    override fun convert(input: String): Boolean {
        return BooleanApplicative.convertOrThrow(input)
    }

}
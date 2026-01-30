package top.lanscarlos.vulpecula.module.bacikal.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.command.subCommand
import top.lanscarlos.vulpecula.common.lang.Lang
import top.lanscarlos.vulpecula.module.bacikal.BacikalRegistry
import top.lanscarlos.vulpecula.module.bacikal.extension.NativeExtension
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.command
 *
 * @author Lanscarlos
 * @since 2025/9/10
 */
object PropertyCommand {

    @CommandBody
    val property = subCommand {
        literal("registry", literal = registry)
    }

    private val registry: CommandComponent.() -> Unit = {
        execute<ProxyCommandSender> { sender, _, _ ->
            displayBacikalProperties(sender, false)
        }

        literal("detail") {
            execute<ProxyCommandSender> { sender, _, _ ->
                displayBacikalProperties(sender, true)
            }
        }
    }

    private fun displayBacikalProperties(sender: ProxyCommandSender, detail: Boolean) {
        val bacikalResolvers = BacikalRegistry.getPropertyResolverValues()
        Lang.BACIKAL_COMMAND_REGISTRY_BACIKAL_PROPERTY.info(sender, bacikalResolvers.size)

        for ((source, parsers) in bacikalResolvers.groupBy { it.extension }) {
            val color = if (source is NativeExtension) "§3" else "§b"
            Lang.BACIKAL_COMMAND_REGISTRY_ITEM.info(
                sender,
                source.name,
                source.version,
                parsers.joinToString("§7, ") { "$color${it.id}" }
            )
        }

        // 显示注册异常的信息
        for (parser in BacikalRegistry.getExceptionalParsers()) {
            Lang.BACIKAL_COMMAND_REGISTRY_EXCEPTIONAL.error(sender, parser.exception.localizedMessage)
        }
    }

}
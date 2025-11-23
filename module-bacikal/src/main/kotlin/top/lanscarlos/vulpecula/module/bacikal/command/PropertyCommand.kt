package top.lanscarlos.vulpecula.module.bacikal.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.command.subCommand
import top.lanscarlos.vulpecula.common.utils.asLang
import top.lanscarlos.vulpecula.module.bacikal.BacikalRegistry
import top.lanscarlos.vulpecula.module.bacikal.error
import top.lanscarlos.vulpecula.module.bacikal.extension.NativeExtension
import top.lanscarlos.vulpecula.module.bacikal.info
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
        sender.info { asLang("module-bacikal-command-registry-display-bacikal-property", bacikalResolvers.size) }
        for ((source, parsers) in bacikalResolvers.groupBy { it.extension }) {
            val color = if (source is NativeExtension) "§3" else "§b"
            sender.info {
                asLang(
                    "module-bacikal-command-registry-display-item",
                    source.name,
                    source.version,
                    parsers.joinToString("§7, ") { "$color${it.id}" }
                )
            }
        }

        // 显示注册异常的信息
        for (parser in BacikalRegistry.getExceptionalParsers()) {
            sender.error { parser.exception.localizedMessage }
        }
    }

}
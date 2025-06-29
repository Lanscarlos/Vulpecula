package top.lanscarlos.vulpecula.module.item

import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.command.subCommand

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.item
 *
 * @author Lanscarlos
 * @since 2025-01-12 14:13
 */
object ItemCommand {

    @CommandBody
    val item = subCommand {
        literal("switch", literal = switch)
    }

    val switch: CommandComponent.() -> Unit = {
    }

}
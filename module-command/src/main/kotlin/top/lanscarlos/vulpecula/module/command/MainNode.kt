package top.lanscarlos.vulpecula.module.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.component.CommandBase
import taboolib.expansion.createHelper
import taboolib.library.configuration.ConfigurationSection

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.command
 *
 * @author Lanscarlos
 * @since 2025/4/29 17:03
 */
class MainNode(section: ConfigurationSection) : LiteralNode("main", null, section) {

    override fun build(): CommandBase {
        val component = CommandBase()
        if (executor == null) {
            // 默认创建命令提示
            component.createHelper()
        } else {
            component.execute(bind = ProxyCommandSender::class.java, function = ::execute)
        }
        for (child in children) {
            component.children += child.build()
        }
        return component
    }

}
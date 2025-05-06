package top.lanscarlos.vulpecula.module.command

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
class MainNode(section: ConfigurationSection) : Node("main", null, section) {

    override fun build(): CommandBase {
        val component = CommandBase()
        component.createHelper()
        for (child in children) {
            component.children += child.build()
        }
        return component
    }

}
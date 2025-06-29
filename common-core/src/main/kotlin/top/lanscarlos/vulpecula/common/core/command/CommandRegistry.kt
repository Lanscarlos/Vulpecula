package top.lanscarlos.vulpecula.common.core.command

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.command.PermissionDefault
import taboolib.common.platform.command.SimpleCommandBody
import taboolib.common.platform.command.command
import taboolib.expansion.createHelper

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.core.command
 *
 * @author Lanscarlos
 * @since 2025/6/29
 */
object CommandRegistry {

    private val components = mutableListOf(
        SimpleCommandBody().also {
            it.name = "develop"
            it.aliases = arrayOf("dev")
            it.permission = "vulpecula.command.develop"
        }
    )

    fun registerDevelopComponent(component: SimpleCommandBody) {
        components.first().children += component
    }

    fun registerCommandComponent(component: SimpleCommandBody) {
        components += component
    }

    @Awake(LifeCycle.ENABLE)
    fun register() {
        command(
            "vulpecula",
            listOf("vul"),
            "Vulpecula main command.",
            "/vul <subcommand>",
            "vulpecula.command",
            "You do not have permission to use this command.",
            PermissionDefault.OP,
            components.filter { it.permission.isNotEmpty() }.associate { it.permission to it.permissionDefault },
            false
        ) {
            createHelper()
            for (component in components) {
                this.literal(component.name, *component.aliases, optional = component.optional, permission = component.permission, hidden = component.hidden) outer@{
                    component.func(this@outer)

                    if (component.name == "develop") {
                        // 处理开发者命令的子命令
                        for (child in component.children) {
                            this@outer.literal(
                                child.name,
                                *child.aliases.filter { it != "@develop" }.toTypedArray(), // 不知道为什么此处莫名其妙多了 @develop 别名
                                optional = child.optional,
                                permission = child.permission,
                                hidden = child.hidden
                            ) inner@{
                                child.func(this@inner)
                            }
                        }
                    }
                }
            }
        }
    }

}
package top.lanscarlos.vulpecula.module.core.command

import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitor
import taboolib.common.platform.Awake
import taboolib.common.platform.command.*
import taboolib.common.platform.function.info
import taboolib.expansion.createHelper
import taboolib.library.reflex.ClassField
import taboolib.library.reflex.ReflexClass

/**
 * FrontierProduction
 * me.asgard.frontier.production.common
 *
 * @author Lanscarlos
 * @since 2024-10-16 11:41
 */
@Awake(LifeCycle.LOAD)
object CommandRegistry : ClassVisitor() {

    private val mainComponents = mutableListOf(
        SimpleCommandBody().also {
            it.name = "develop"
            it.aliases = arrayOf("dev")
            it.permission = "vulpecula.command.develop"
        }
    )

    override fun getLifeCycle() = LifeCycle.LOAD

    override fun visit(field: ClassField, owner: ReflexClass) {
        val clazz = owner.toClass()
        if (clazz.isAnnotationPresent(CommandHeader::class.java)) {
            // 排除正常命令注册的情况
            return
        }
        if (!field.isAnnotationPresent(CommandBody::class.java)) {
            return
        }
        if (field.fieldType != SimpleCommandBody::class.java) {
            return
        }
        val annotation = field.getAnnotation(CommandBody::class.java)
        val component = field.get(owner.getInstance()) as SimpleCommandBody
        component.name = field.name
        component.aliases = annotation.list<String>("aliases").toTypedArray()
        component.optional = annotation.property("optional", false)
        component.permission = annotation.property("permission", "")
        component.permissionDefault = annotation.enum("permissionDefault", PermissionDefault.OP)
        component.hidden = annotation.property("hidden", false)

        if (component.aliases.firstOrNull() == "@DEVELOP") {
            // 注册到开发者命令之下
            component.aliases = if (component.aliases.size > 1) {
                component.aliases.sliceArray(1 until component.aliases.size)
            } else {
                emptyArray()
            }
            mainComponents.first().children += component
        } else {
            mainComponents += component
        }

        info("Register vulpecula command component: ${component.name}. aliases: ${component.aliases.joinToString(", ")}")
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
            mainComponents.filter { it.permission.isNotEmpty() }.associate { it.permission to it.permissionDefault },
            false
        ) {
            createHelper()
            for (component in mainComponents) {
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
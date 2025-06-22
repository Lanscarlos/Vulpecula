package top.lanscarlos.vulpecula.command

import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitor
import taboolib.common.platform.Awake
import taboolib.common.platform.command.*
import taboolib.common.platform.function.info
import taboolib.expansion.createHelper
import taboolib.library.reflex.ClassField
import java.util.function.Supplier

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.command
 *
 * Vulpecula 主命令
 *
 * @author Lanscarlos
 * @since 2024-05-14 15:16
 */
@Awake(LifeCycle.LOAD)
object VulpeculaCommand : ClassVisitor() {

    private val components = mutableListOf<SimpleCommandBody>()

    override fun getLifeCycle() = LifeCycle.LOAD

    override fun visit(field: ClassField, clazz: Class<*>, instance: Supplier<*>?) {
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
        val component = field.get(instance?.get()) as SimpleCommandBody
        component.name = field.name
        component.aliases = annotation.property("aliases", emptyArray())
        component.optional = annotation.property("optional", false)
        component.permission = annotation.property("permission", "")
        component.permissionDefault = annotation.enum("permissionDefault", PermissionDefault.OP)
        component.hidden = annotation.property("hidden", false)
        components += component
        info("Register command component: ${component.name}")
    }

    @Awake(LifeCycle.ENABLE)
    fun register() {
        command(
            "vulpecula",
            listOf("vul"),
            "Vulpecula main command",
            "/vulpecula <subcommand>",
            "vulpecula.command",
            "You do not have permission to use this command.",
            PermissionDefault.OP,
            components.filter { it.permission.isNotEmpty() }.associate { it.permission to it.permissionDefault },
            false
        ) {
            createHelper()
            for (component in components) {
                this.literal(component.name, *component.aliases, optional = component.optional, permission = component.permission, hidden = component.hidden) {
                    component.func(this@literal)
                }
            }
        }
    }
}
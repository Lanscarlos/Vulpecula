package top.lanscarlos.vulpecula.common.core.command

import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitor
import taboolib.common.platform.Awake
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.PermissionDefault
import taboolib.common.platform.command.SimpleCommandBody
import taboolib.common.platform.function.info
import taboolib.library.reflex.ClassField
import taboolib.library.reflex.ReflexClass

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.core.command
 *
 * @author Lanscarlos
 * @since 2025/6/29
 */
@Awake(LifeCycle.LOAD)
object CommandScanner : ClassVisitor() {

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
            CommandRegistry.registerDevelopComponent(component)
        } else {
            CommandRegistry.registerCommandComponent(component)
        }

        info("Register vulpecula command component: ${component.name}. aliases: ${component.aliases.joinToString(", ")}")
    }

    override fun getLifeCycle(): LifeCycle {
        return LifeCycle.LOAD
    }

}
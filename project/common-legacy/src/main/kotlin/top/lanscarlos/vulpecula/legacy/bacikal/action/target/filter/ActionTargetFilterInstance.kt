package top.lanscarlos.vulpecula.legacy.bacikal.action.target.filter

import taboolib.common.platform.function.warning
import top.lanscarlos.vulpecula.legacy.bacikal.action.target.ActionTarget

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.target.filter
 *
 * @author Lanscarlos
 * @since 2023-03-23 11:47
 */
object ActionTargetFilterInstance : ActionTarget.Resolver {

    override val name: Array<String> = arrayOf("instance", "inst")

    override fun resolve(reader: ActionTarget.Reader): ActionTarget.Handler<out Any?> {
        return reader.transfer {
            combine(
                source(),
                trim("with", then = text(display = "instance type"))
            ) { target, instance ->

                // 获取类类型
                val clazz = try {
                    Class.forName(instance)
                } catch (e: Exception) {
                    warning(e.localizedMessage)
                    return@combine target
                }

                val iterator = target.iterator()
                while (iterator.hasNext()) {
                    if (!clazz.isAssignableFrom(iterator.next().javaClass)) {
                        // 不属于该类型
                        iterator.remove()
                    }
                }

                target
            }
        }
    }
}
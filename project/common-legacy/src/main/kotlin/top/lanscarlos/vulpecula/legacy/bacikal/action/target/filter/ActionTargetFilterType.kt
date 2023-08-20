package top.lanscarlos.vulpecula.legacy.bacikal.action.target.filter

import org.bukkit.entity.Entity
import top.lanscarlos.vulpecula.legacy.bacikal.action.target.ActionTarget

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.target.filter
 *
 * @author Lanscarlos
 * @since 2023-03-22 23:04
 */
object ActionTargetFilterType : ActionTarget.Resolver {

    override val name: Array<String> = arrayOf("type")

    override fun resolve(reader: ActionTarget.Reader): ActionTarget.Handler<out Any?> {
        return reader.transfer {
            combine(
                source(),
                trim("with", then = list())
            ) { target, types ->

                val include = mutableSetOf<String>()
                val exclude = mutableSetOf<String>()

                for (element in types) {
                    val type = element?.toString()?.uppercase()?.replace('-', '_') ?: continue
                    if (type[0] == '!') {
                        // 排除
                        exclude += type
                    } else {
                        // 包含
                        include += type
                    }
                }

                val iterator = target.iterator()
                while (iterator.hasNext()) {
                    val type = when (val it = iterator.next()) {
                        is Entity -> it.type.name
                        is org.bukkit.Location,
                        is taboolib.common.util.Location -> "location"
                        else -> {
                            // 未知类型
                            iterator.remove()
                            continue
                        }
                    }

                    // 排除类型不为空 且 属于排除类型
                    if (exclude.isNotEmpty() && type in exclude) iterator.remove()

                    // 包含类型不为空 且 不属于包含类型
                    if (include.isNotEmpty() && type !in include) iterator.remove()
                }

                target
            }
        }
    }
}
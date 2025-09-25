package top.lanscarlos.vulpecula.module.action.target

import org.bukkit.entity.Entity
import top.lanscarlos.vulpecula.module.bacikal.annotation.Parser
import top.lanscarlos.vulpecula.module.bacikal.parser.BacikalFrame
import top.lanscarlos.vulpecula.module.bacikal.parser.ClassActionResolver

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.action.target
 *
 * @author Lanscarlos
 * @since 2025/9/24
 */
@Parser("target.filter.type")
object ActionTargetFilterType : ClassActionResolver {

    fun resolve(
        frame: BacikalFrame,
        types: String
    ) {
        val include = mutableSetOf<String>()
        val exclude = mutableSetOf<String>()

        for (type in types.split(',')) {
            if (type[0] == '!') {
                exclude.add(type.substring(1).uppercase())
            } else {
                include.add(type.uppercase())
            }
        }

        val targets = mutableListOf<Any>()
        for (target in ActionTarget.getContext(frame)) {
            val type = when (target) {
                is Entity -> target.type.name
                is org.bukkit.Location,
                is taboolib.common.util.Location -> "LOCATION"
                else -> error("Unsupported target type: ${target.javaClass.name}")
            }
            if (exclude.isNotEmpty() && type in exclude) {
                // 排除
                continue
            }
            if (include.isNotEmpty() && type !in include) {
                continue
            }
            targets.add(target)
        }

        ActionTarget.setContext(frame, targets)
    }

}
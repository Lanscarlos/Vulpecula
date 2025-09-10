package top.lanscarlos.vulpecula.module.action.entity

import org.bukkit.entity.Entity
import top.lanscarlos.vulpecula.module.bacikal.annotation.Parser
import top.lanscarlos.vulpecula.module.bacikal.parser.BacikalFrame
import top.lanscarlos.vulpecula.module.bacikal.parser.ClassActionResolver

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.action.entity
 *
 * @author Lanscarlos
 * @since 2025/6/29
 */
@Parser("entity.switch")
object ActionEntitySwitch : ClassActionResolver {

    fun resolve(frame: BacikalFrame, entity: Entity) {
        ActionEntity.setContext(frame, entity)
    }

}
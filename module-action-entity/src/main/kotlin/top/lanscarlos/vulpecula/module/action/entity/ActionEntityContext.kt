package top.lanscarlos.vulpecula.module.action.entity

import org.bukkit.entity.Entity
import top.lanscarlos.vulpecula.module.bacikal.annotation.BacikalParser
import top.lanscarlos.vulpecula.module.bacikal.parser.BacikalFrame
import top.lanscarlos.vulpecula.module.bacikal.parser.ClassActionResolver

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.action.entity
 *
 * @author Lanscarlos
 * @since 2025/6/29
 */
@BacikalParser("entity.context")
object ActionEntityContext : ClassActionResolver {

    fun resolve(frame: BacikalFrame): Entity {
        return ActionEntity.getContext(frame)
    }

}
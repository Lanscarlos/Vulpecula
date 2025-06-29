package top.lanscarlos.vulpecula.module.action.entity

import org.bukkit.entity.Entity
import top.lanscarlos.vulpecula.common.core.utils.asLang
import top.lanscarlos.vulpecula.module.bacikal.parser.BacikalFrame

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.action.entity
 *
 * @author Lanscarlos
 * @since 2025/6/29
 */
object ActionEntity {

    private const val CONTEXT = "@ENTITY"

    fun getEntity(frame: BacikalFrame): Entity {
        val value = frame.getVariable<Entity>(CONTEXT)
        if (value != null) {
            return value
        }

        val player = frame.senderAsPlayer
        require(player != null) {
            asLang("module-action-entity-exception-entity-not-found")
        }
        setEntity(frame, player)
        return player
    }

    fun setEntity(frame: BacikalFrame, entity: Entity) {
        frame.setVariable(CONTEXT, entity)
    }

}
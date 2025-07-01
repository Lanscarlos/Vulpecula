package top.lanscarlos.vulpecula.module.action.entity

import org.bukkit.entity.Entity
import top.lanscarlos.vulpecula.common.core.utils.asLang
import top.lanscarlos.vulpecula.module.bacikal.action.ExternalAction
import top.lanscarlos.vulpecula.module.bacikal.parser.BacikalFrame

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.action.entity
 *
 * @author Lanscarlos
 * @since 2025/6/29
 */
object ActionEntity : ExternalAction() {

    private const val CONTEXT = "@ENTITY"

    fun getContext(frame: BacikalFrame): Entity {
        val value = frame.getVariable<Entity>(CONTEXT)
        if (value != null) {
            return value
        }

        val player = frame.senderAsPlayer
        require(player != null) {
            asLang("module-action-entity-exception-entity-not-found")
        }
        setContext(frame, player)
        return player
    }

    fun setContext(frame: BacikalFrame, entity: Entity) {
        frame.setVariable(CONTEXT, entity)
    }

}
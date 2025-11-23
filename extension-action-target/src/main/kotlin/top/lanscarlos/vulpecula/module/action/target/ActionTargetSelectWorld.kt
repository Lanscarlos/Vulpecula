package top.lanscarlos.vulpecula.module.action.target

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import top.lanscarlos.vulpecula.common.utils.asLang
import top.lanscarlos.vulpecula.module.bacikal.annotation.Additional
import top.lanscarlos.vulpecula.module.bacikal.annotation.Parser
import top.lanscarlos.vulpecula.module.bacikal.parser.BacikalFrame
import top.lanscarlos.vulpecula.module.bacikal.parser.ClassActionResolver

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.action.target
 *
 * @author Lanscarlos
 * @since 2025/9/3
 */
@Parser("target.select.world")
object ActionTargetSelectWorld : ClassActionResolver {

    fun resolve(
        frame: BacikalFrame,
        worldName: String,
        @Additional(["playerOnly"]) playerOnly: Boolean = true
    ) {
        val world = Bukkit.getWorld(worldName) ?: error(asLang("module-action-target-exception-world-not-found", worldName))
        val entities = if (playerOnly) {
            world.entities.filterIsInstance<Player>()
        } else {
            world.entities
        }
        ActionTarget.setContext(frame, entities)
    }

}
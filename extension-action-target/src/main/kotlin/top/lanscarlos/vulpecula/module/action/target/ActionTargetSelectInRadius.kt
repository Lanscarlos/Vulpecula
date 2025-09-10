package top.lanscarlos.vulpecula.module.action.target

import org.bukkit.Location
import org.bukkit.entity.Player
import top.lanscarlos.vulpecula.module.bacikal.annotation.Additional
import top.lanscarlos.vulpecula.module.bacikal.annotation.Optional
import top.lanscarlos.vulpecula.module.bacikal.annotation.Parser
import top.lanscarlos.vulpecula.module.bacikal.parser.BacikalFrame
import top.lanscarlos.vulpecula.module.bacikal.parser.ClassActionResolver
import java.util.LinkedList

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.action.target
 *
 * @author Lanscarlos
 * @since 2025/9/6
 */
@Parser("target.select.in-radius")
object ActionTargetSelectInRadius : ClassActionResolver {

    /**
     * target select in-radius 5.0 at 0,0,0 --includeSelf true
     * */
    fun resolve(
        frame: BacikalFrame,
        sender: Player,
        radius: Double,
        @Optional(["at"]) location: Location = sender.location,
        @Additional(["includeSelf"]) includeSelf: Boolean = false
    ) {
        val world = location.world ?: sender.world
        val radiusSquared = radius * radius
        val entities = world.getNearbyEntities(location, radius, radius, radius) {
            if (!includeSelf && it == sender) {
                // 排除自身
                return@getNearbyEntities false
            }
            // 确保距离在半径球体内
            sender.location.distanceSquared(it.location) <= radiusSquared
        }
        ActionTarget.setContext(frame, LinkedList<Any>(entities))
    }

}
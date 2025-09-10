package top.lanscarlos.vulpecula.module.action.target

import org.bukkit.Location
import org.bukkit.entity.Player
import taboolib.common.util.Vector
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
@Parser("target.select.in-box")
object ActionTargetSelectInBox : ClassActionResolver {

    /**
     * target select in-box 5,2,3 at 0,0,0 --includeSelf true
     * */
    fun resolve(
        frame: BacikalFrame,
        sender: Player,
        box: Vector,
        @Optional(["at"]) location: Location = sender.location,
        @Additional(["includeSelf"]) includeSelf: Boolean = false
    ) {
        val world = location.world ?: sender.world
        val radiusX = box.x
        val radiusY = box.y
        val radiusZ = box.z
        val entities = world.getNearbyEntities(location, radiusX, radiusY, radiusZ) {
            // 排除自身
            includeSelf || it != sender
        }
        ActionTarget.setContext(frame, LinkedList<Any>(entities))
    }

}
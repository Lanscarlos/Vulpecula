package top.lanscarlos.vulpecula.module.action.entity

import taboolib.common.util.Location
import taboolib.platform.util.toBukkitLocation
import top.lanscarlos.vulpecula.module.bacikal.annotation.Parser
import top.lanscarlos.vulpecula.module.bacikal.annotation.Expected
import top.lanscarlos.vulpecula.module.bacikal.parser.BacikalFrame
import top.lanscarlos.vulpecula.module.bacikal.parser.ClassActionResolver

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.action.entity
 *
 * @author Lanscarlos
 * @since 2025/7/3
 */
@Parser("entity.teleport")
object ActionEntityTeleport : ClassActionResolver {

    fun resolve(frame: BacikalFrame, @Expected(["to"]) location: Location) {
        val entity = ActionEntity.getContext(frame)
        entity.teleport(location.toBukkitLocation())
    }

}
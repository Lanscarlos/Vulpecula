package top.lanscarlos.vulpecula.module.action.illusion

import org.bukkit.Location
import org.bukkit.entity.Player
import top.lanscarlos.vulpecula.module.bacikal.annotation.Additional
import top.lanscarlos.vulpecula.module.bacikal.annotation.BacikalParser
import top.lanscarlos.vulpecula.module.bacikal.parser.ClassActionResolver
import top.lanscarlos.vulpecula.module.volatility.VolatileWorldBorder

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.action.illusion
 *
 * @author Lanscarlos
 * @since 2025/7/7
 */
@BacikalParser("illusion.worldborder")
object ActionIllusionWorldBorder : ClassActionResolver {

    fun resolve(
        viewer: Player,
        @Additional(["size"]) size: Double?,
        @Additional(["center"]) center: Location?,
        @Additional(["warningTime"]) warningTime: Int?,
        @Additional(["warningDistance"]) warningDistance: Int?,
        @Additional(["damageBuffer"]) damageBuffer: Double?,
        @Additional(["damageAmount"]) damageAmount: Double?,
    ) {
        VolatileWorldBorder.sendWorldBorder(viewer, size, center, warningTime, warningDistance, damageBuffer, damageAmount)
    }

}
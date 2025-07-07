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
        @Additional(["size"]) size: Double = viewer.worldBorder?.size ?: 0.0,
        @Additional(["centerX", "x"]) centerX: Double = viewer.worldBorder?.center?.x ?: 0.0,
        @Additional(["centerZ", "z"]) centerZ: Double = viewer.worldBorder?.center?.z ?: 0.0,
        @Additional(["warningTime"]) warningTime: Int = viewer.worldBorder?.warningTime ?: 0,
        @Additional(["warningDistance"]) warningDistance: Int = viewer.worldBorder?.warningDistance ?: 0,
        @Additional(["damageBuffer"]) damageBuffer: Double = viewer.worldBorder?.damageBuffer ?: 0.0,
        @Additional(["damageAmount"]) damageAmount: Double = viewer.worldBorder?.damageAmount ?: 0.0,
    ) {
        val center = Location(null, centerX, 0.0, centerZ)
        VolatileWorldBorder.sendWorldBorder(viewer, center, size, warningTime, warningDistance, damageBuffer, damageAmount)
    }

}
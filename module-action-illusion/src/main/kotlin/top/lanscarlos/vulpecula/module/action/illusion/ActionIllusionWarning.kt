package top.lanscarlos.vulpecula.module.action.illusion

import org.bukkit.entity.Player
import top.lanscarlos.vulpecula.module.bacikal.annotation.BacikalParser
import top.lanscarlos.vulpecula.module.bacikal.parser.ClassActionResolver
import top.lanscarlos.vulpecula.module.volatility.VolatileWorldBorder
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.action.illusion
 *
 * @author Lanscarlos
 * @since 2025/7/8 10:43
 */
object ActionIllusionWarning {

    private val effect: ConcurrentHashMap<UUID, WarningEffect> = ConcurrentHashMap()

    fun getEffect(player: Player): WarningEffect? {
        return effect[player.uniqueId]
    }

    fun sendWarningEffect(viewer: Player, level: Int) {
        require(level in 0..100) { "level must be between 0 and 100" }
        val warningDistance = (1024000.0 / (101 - level.toDouble())).toInt()
        VolatileWorldBorder.sendWorldBorder(
            viewer,
            size = 10240.0,
            center = viewer.location,
            warningTime = null,
            warningDistance = warningDistance,
            damageBuffer = null,
            damageAmount = null
        )
    }

}

@BacikalParser("illusion.warning.set")
object ActionIllusionWarningSet : ClassActionResolver {

    fun resolve(viewer: Player, level: Int) {
        ActionIllusionWarning.sendWarningEffect(viewer, level)
    }

}
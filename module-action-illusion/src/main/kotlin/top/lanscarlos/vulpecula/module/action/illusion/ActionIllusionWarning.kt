package top.lanscarlos.vulpecula.module.action.illusion

import org.bukkit.entity.Player
import top.lanscarlos.vulpecula.module.bacikal.annotation.Additional
import top.lanscarlos.vulpecula.module.bacikal.annotation.BacikalParser
import top.lanscarlos.vulpecula.module.bacikal.annotation.Optional
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

    private val effects: ConcurrentHashMap<UUID, WarningEffect> = ConcurrentHashMap()

    fun getEffect(viewer: Player): WarningEffect? {
        return effects[viewer.uniqueId]
    }

    fun setEffect(viewer: Player, effect: WarningEffect) {
        effects[viewer.uniqueId]?.stop()
        effects[viewer.uniqueId] = effect
        effect.apply()
    }

    fun clearEffect(viewer: Player) {
        effects.remove(viewer.uniqueId)?.stop()
    }

    fun sendWarningEffect(viewer: Player, level: Int) {
        require(level in 0..100) { "level must be between 0 and 100" }
        val warningDistance = if (level > 0) {
            (1024000.0 / (101 - level)).toInt()
        } else {
            1024
        }
        // 线性分级 0-100
        VolatileWorldBorder.sendWorldBorder(
            viewer,
            size = 20480.0,
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

    fun resolve(viewer: Player, level: Int, @Optional(["in"]) duration: Int = 20) {
        ActionIllusionWarning.setEffect(viewer, FadeInWarningEffect(viewer,  level, duration))
    }

}

@BacikalParser("illusion.warning.breathing")
object ActionIllusionWarningBreathing : ClassActionResolver {

    fun resolve(viewer: Player, @Additional(["speed"]) speed: Int = 5) {
        ActionIllusionWarning.setEffect(viewer, BreathingWarningEffect(viewer, speed.toDouble()))
    }

}

@BacikalParser("illusion.warning.clear")
object ActionIllusionWarningClear : ClassActionResolver {

    fun resolve(viewer: Player) {
        ActionIllusionWarning.clearEffect(viewer)
    }

}
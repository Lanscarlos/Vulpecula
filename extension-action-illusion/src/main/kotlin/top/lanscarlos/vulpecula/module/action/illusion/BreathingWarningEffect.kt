package top.lanscarlos.vulpecula.module.action.illusion

import org.bukkit.entity.Player
import taboolib.common.platform.function.submit
import taboolib.common.platform.service.PlatformExecutor
import kotlin.math.asin
import kotlin.math.sin

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.action.illusion
 *
 * @author Lanscarlos
 * @since 2025/7/8 14:20
 */
class BreathingWarningEffect(val viewer: Player, val speed: Double) : WarningEffect {

    override var level: Int

    private var degrees : Double

    private lateinit var task: PlatformExecutor.PlatformTask

    init {
        require(speed > 0.0) { "Step must be positive, was: $speed" }
        val effect = ActionIllusionWarning.getEffect(viewer)
        level = effect?.level ?: 0
        degrees = asin((level / 50.0) - 1) * 180.0 / Math.PI
    }

    override fun apply() {
        task = submit(period = 1) {
            onTick()
            ActionIllusionWarning.sendWarningEffect(viewer, level)
        }
    }

    override fun stop() {
        if (::task.isInitialized) {
            task.cancel()
        }
    }

    private fun onTick() {
        val amplifier = sin(Math.toRadians(degrees)) + 1.0
        level = (50.0 * amplifier).toInt().coerceIn(0..100)
        degrees += speed
        if (degrees >= 360.0) {
            degrees = 0.0 // 重置角度
        }
    }

}
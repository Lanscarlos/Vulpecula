package top.lanscarlos.vulpecula.module.action.illusion

import org.bukkit.entity.Player
import taboolib.common.platform.function.submit
import taboolib.common.platform.service.PlatformExecutor

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.action.illusion
 *
 * @author Lanscarlos
 * @since 2025/7/8 14:20
 */
class BreathingWarningEffect(val viewer: Player, var step: Int) : WarningEffect {

    override var level: Int

    private lateinit var task: PlatformExecutor.PlatformTask

    init {
        val effect = ActionIllusionWarning.getEffect(viewer)
        level = effect?.level ?: 0
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
        level = (level + step)
        level = if (level <= 0) {
            0
        } else if (level >= 100) {
            100
        } else {
            return
        }
        step = -step
    }

}
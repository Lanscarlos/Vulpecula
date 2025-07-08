package top.lanscarlos.vulpecula.module.action.illusion

import org.bukkit.entity.Player
import taboolib.common.platform.function.submit
import taboolib.common.platform.service.PlatformExecutor

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.action.illusion
 *
 * @param duration 淡入时间, tick
 *
 * @author Lanscarlos
 * @since 2025/7/8 14:52
 */
class FadeInWarningEffect(private val viewer: Player, private val target: Int, private val duration: Int) : WarningEffect {

    override var level: Int

    private val step: Long

    private var current: Long

    private var timer: Int = 0

    private lateinit var task: PlatformExecutor.PlatformTask

    init {
        val effect = ActionIllusionWarning.getEffect(viewer)
        level = effect?.level ?: 0
        current = level.toLong() shl 32
        step = ((target - level).toLong() shl 32) / duration
    }

    override fun apply() {
        if (level == target) {
            return
        }
        task = submit(period = 1) {
            if (timer++ >= duration) {
                level = target
                cancel()
                return@submit
            }
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
        current += step
        level = (current shr 32).toInt()
    }

}
package top.lanscarlos.vulpecula.module.dispatcher.pipeline.player

import org.bukkit.Location
import org.bukkit.event.player.PlayerMoveEvent
import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.common.config.boolean
import top.lanscarlos.vulpecula.common.config.read
import top.lanscarlos.vulpecula.module.dispatcher.Context
import top.lanscarlos.vulpecula.module.dispatcher.pipeline.AbstractPipeline
import top.lanscarlos.vulpecula.module.dispatcher.pipeline.AutoRegistered

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.dispatcher.pipeline.player
 *
 * @author Lanscarlos
 * @since 2025/6/15
 */
@AutoRegistered
class PlayerMoveEventPipeline(clazz: Class<*>, config: ConfigurationSection) : AbstractPipeline<PlayerMoveEvent>(clazz, config) {

    /**
     * 是否捕捉视角变化, 默认情况下, 玩家只移动视角也会触发该事件
     * */
    val captureViewChange: Boolean by config.read("capture-view-change").boolean(false)

    /**
     * 是否捕捉细微的移动, 默认下只监听玩家从一个方块移动到另一个方块的变动
     * */
    val captureSubtleMovement: Boolean by config.read("capture-subtle-movement").boolean(false)

    override fun filter(context: Context) {
        if (context.event::class.java != PlayerMoveEvent::class.java) {
            // 可能为 PlayerMoveEvent 的子类, 不做处理, 放行
            return
        }
        val event = getEvent(context)
        val from = event.from
        val to = event.to ?: return
        if (from.world != to.world) {
            // 世界不一致, 放行
            return
        }
        when {
            captureViewChange -> {
                // 直接放行
                return
            }
            captureSubtleMovement -> {
                if (!checkDisplacement(from, to)) {
                    // 不存在位移, 过滤事件
                    context.filter()
                    return
                }
            }
            else -> {
                if (!checkCrossBlock(from, to)) {
                    // 没有跨越方块, 过滤事件
                    context.filter()
                    return
                }
            }
        }
    }

    private fun checkCrossBlock(from: Location, to: Location): Boolean {
        return from.blockX != to.blockX || from.blockY != to.blockY || from.blockZ != to.blockZ
    }

    /**
     * 检测玩家是否产生实际位移
     *
     * @return 若有位移则返回 true, 反之 false
     * */
    private fun checkDisplacement(from: Location, to: Location): Boolean {
        if (from.x - to.x > 1e-3) {
            return true
        }
        if (from.y - to.y > 1e-3) {
            return true
        }
        if (from.z - to.z > 1e-3) {
            return true
        }
        return false
    }

}
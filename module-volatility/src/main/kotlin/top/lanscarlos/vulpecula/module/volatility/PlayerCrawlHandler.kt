package top.lanscarlos.vulpecula.module.volatility

import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.entity.Pose
import org.bukkit.event.player.PlayerMoveEvent
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.command.suggestPlayers
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.library.reflex.Reflex.Companion.invokeMethod
import taboolib.platform.util.getMetaFirstOrNull
import taboolib.platform.util.removeMeta
import taboolib.platform.util.setMeta

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.volatility
 *
 * @author Lanscarlos
 * @since 2025/7/6
 */
object PlayerCrawlHandler {

    /** 元数据标签：标识玩家是否处于爬行状态 */
    private const val META_CRAWLING = "CRAWLING"

    @CommandBody
    val craw = subCommand {
        execute<Player> { sender, _, _ ->
            setCrawling(sender, true)
        }


        dynamic {
            suggestPlayers()
            execute<Player> { sender, _, name ->
                val player = Bukkit.getPlayerExact(name)!!
                setCrawling(player, true)
            }
        }
    }

    /**
     * 爬行状态数据类，用于管理玩家的爬行状态及方块显示。
     */
    private class CrawlState(private val player: Player, private var lastBlockLocation: Location? = null) {

        init {
            player.gameMode = GameMode.ADVENTURE
            updateBlockState(player.location)
        }

        /**
         * 更新爬行状态对应的方块显示。
         *
         * @param target 新的目标位置
         */
        fun updateBlockState(target: Location) {
            val currentBlockLocation = target.block.location.apply { y += 1 }

            // 跳过固体方块或未移动的方块
            if (currentBlockLocation.block.type.isSolid || currentBlockLocation == lastBlockLocation) return

            // 恢复之前方块显示
            lastBlockLocation?.let { player.sendBlockChange(it, it.block.blockData) }
            lastBlockLocation = currentBlockLocation

            // 设置新位置的 Trapdoor 方块
//            val blockFraction = target.y - target.blockY
            val newBlockData = Material.BARRIER.createBlockData()
//            val half = if (blockFraction < 0.40) Bisected.Half.BOTTOM else Bisected.Half.TOP
//            val trapdoorData = Material.ACACIA_TRAPDOOR.createBlockData { data ->
//                (data as TrapDoor).half = half
//            }
            player.sendBlockChange(currentBlockLocation, newBlockData)
        }

        /**
         * 清理爬行状态，恢复最后位置的方块显示。
         */
        fun clearState() {
            lastBlockLocation?.let { player.sendBlockChange(it, it.block.blockData) }
        }
    }

    /**
     * 判断玩家是否处于爬行状态。
     *
     * @param player 玩家对象
     * @return 如果玩家处于爬行状态则返回 true，否则返回 false
     */
    fun isCrawling(player: Player): Boolean {
        return player.pose == Pose.SWIMMING
    }

    /**
     * 设置玩家的爬行状态。
     *
     * @param player 玩家对象
     * @param enable 是否启用爬行状态
     */
    fun setCrawling(player: Player, enable: Boolean) {
        if (enable) {
            player.invokeMethod<Void>("setPose", Pose.SWIMMING, true)
            player.setMeta(META_CRAWLING, CrawlState(player))
        } else {
            player.getCrawlState()?.clearState()
            player.invokeMethod<Void>("setPose", Pose.STANDING, false)
            player.removeMeta(META_CRAWLING)
        }
    }

    /**
     * 玩家移动事件监听器，用于实时更新爬行状态。
     *
     * 如果玩家的方块位置发生变化，将更新爬行状态或退出爬行模式。
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        val crawlState = player.getCrawlState() ?: return
        val newLocation = event.to ?: return

        if (event.from.block != newLocation.block) {
            synchronizeCrawlState(player, crawlState, newLocation)
        }
    }

    /**
     * 同步爬行状态，确保状态与玩家位置保持一致。
     *
     * @param player 玩家对象
     * @param state 爬行状态对象
     * @param location 玩家当前位置
     */
    private fun synchronizeCrawlState(player: Player, state: CrawlState, location: Location) {
        if (isCrawling(player)) {
            state.updateBlockState(location)
        } else {
            setCrawling(player, false)
        }
    }

    /**
     * 获取玩家的爬行状态对象。
     *
     * @return 如果玩家处于爬行状态则返回对应的 CrawlState 对象，否则返回 null
     */
    private fun Player.getCrawlState(): CrawlState? {
        return getMetaFirstOrNull(META_CRAWLING)?.value() as? CrawlState
    }
}
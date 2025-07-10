package top.lanscarlos.vulpecula.module.dispatcher.event

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.submit
import taboolib.platform.type.BukkitProxyEvent
import top.lanscarlos.vulpecula.common.config.bindConfig
import top.lanscarlos.vulpecula.common.config.boolean
import top.lanscarlos.vulpecula.common.config.int
import java.util.EnumMap

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.dispatcher.event
 *
 * @author Lanscarlos
 * @since 2025/7/10 11:47
 */
class PlayerArmorChangeEvent(val slot: EquipmentSlot, val oldItem: ItemStack, var newItem: ItemStack) : BukkitProxyEvent() {

    companion object {

        val enable by bindConfig("player-armor-change-event.enable").boolean(false)

        val period by bindConfig("player-armor-change-event.period").int(5)

        val ITEM_AIR = ItemStack(Material.AIR)

        val cache: HashMap<Player, EnumMap<EquipmentSlot, ItemStack>> = hashMapOf()

        @Awake(LifeCycle.ACTIVE)
        fun onActive() {
            if (!enable) {
                return
            }
            submit(period = period.toLong()) {
                onTick()
            }
        }

        private fun onTick() {
            for (player in Bukkit.getOnlinePlayers()) {
                for (slot in EquipmentSlot.entries) {
                    if (slot == EquipmentSlot.HAND || slot == EquipmentSlot.OFF_HAND) {
                        continue
                    }
                    val oldItem = cache.computeIfAbsent(player) { EnumMap(EquipmentSlot::class.java) }[slot] ?: ITEM_AIR
                    val newItem = player.equipment?.getItem(slot) ?: ITEM_AIR
                    if (!matchItem(oldItem, newItem)) {
                        continue
                    }
                    val event = PlayerArmorChangeEvent(slot, oldItem, newItem)
                    if (!event.call()) {
                        // 事件被取消, 回滚旧物品
                        player.equipment!!.setItem(slot, oldItem)
                        return
                    }
                    // 替换新的物品
                    player.equipment!!.setItem(slot, event.newItem)
                }
            }
        }

        private fun matchItem(oldItem: ItemStack, newItem: ItemStack): Boolean {
            if (oldItem.type != newItem.type) {
                return false
            }
            if (oldItem.itemMeta?.displayName != newItem.itemMeta?.displayName) {
                return false
            }
            // TODO 是否需要自行做 item 的匹配算法
            // name、lore、flags、enchantment、unbreakable、nbt
            if (!oldItem.isSimilar(newItem)) {
                return false
            }
            return true
        }

    }

}
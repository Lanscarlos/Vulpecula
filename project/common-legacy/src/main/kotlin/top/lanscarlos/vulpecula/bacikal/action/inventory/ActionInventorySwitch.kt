package top.lanscarlos.vulpecula.bacikal.action.inventory

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.block.Block
import org.bukkit.block.Container
import org.bukkit.entity.ChestedHorse
import org.bukkit.entity.HumanEntity
import org.bukkit.inventory.ItemStack
import taboolib.platform.type.BukkitPlayer
import top.lanscarlos.vulpecula.utils.playerOrNull
import top.lanscarlos.vulpecula.utils.setVariable
import top.lanscarlos.vulpecula.utils.toBukkit

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.inventory
 *
 * @author Lanscarlos
 * @since 2023-08-06 19:44
 */
object ActionInventorySwitch : ActionInventory.Resolver {

    override val name = arrayOf("switch", "select", "sel")

    override fun resolve(reader: ActionInventory.Reader): ActionInventory.Handler<out Any?> {
        return reader.handle {
            combine(
                any()
            ) { target ->
                val inventory = when (target) {
                    is HumanEntity -> {
                        target.inventory
                    }

                    is BukkitPlayer -> {
                        target.player.inventory
                    }

                    is OfflinePlayer -> {
                        target.player?.inventory
                    }

                    is String -> {
                        when {
                            target.matches("^([A-Za-z0-9_\\- \\u4e00-\\u9fa5]+,)?-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?\$".toRegex()) -> {
                                // x,y,z
                                // world,x,y,z
                                val demand = target.split(",")
                                val holder = if (demand.size == 4) {
                                    org.bukkit.Location(
                                        Bukkit.getWorld(demand[0]) ?: return@combine false,
                                        demand[1].toDouble(),
                                        demand[2].toDouble(),
                                        demand[3].toDouble()
                                    ).block.state
                                } else {
                                    org.bukkit.Location(
                                        playerOrNull()?.toBukkit()?.world ?: return@combine false,
                                        demand[0].toDouble(),
                                        demand[1].toDouble(),
                                        demand[2].toDouble()
                                    ).block.state
                                }

                                if (holder is Container) {
                                    // 保存背包持有者，用于更新方块
                                    this.setVariable("@InventoryHolder", holder, deep = false)
                                    holder.inventory
                                } else {
                                    null
                                }
                            }

                            else -> {
                                Bukkit.getPlayerExact(target)?.inventory
                            }
                        }
                    }

                    is Block -> {
                        val holder = target.state
                        if (holder is Container) {
                            // 保存背包持有者，用于更新方块
                            this.setVariable("@InventoryHolder", holder, deep = false)
                            holder.snapshotInventory
                        } else {
                            null
                        }
                    }

                    is ChestedHorse -> {
                        if (target.isCarryingChest) {
                            // 此马已装备箱子
                            target.inventory
                        } else {
                            null
                        }
                    }

                    is ItemStack -> {
                        // TODO: 2023-08-06 19:45
                        null
                    }

                    else -> null
                } ?: return@combine false

                this.setVariable("@Inventory", inventory, deep = false)
                return@combine true
            }
        }
    }
}
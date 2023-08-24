package top.lanscarlos.vulpecula.applicative

import org.bukkit.Bukkit
import org.bukkit.entity.HumanEntity
import org.bukkit.inventory.Inventory
import taboolib.platform.type.BukkitPlayer

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.applicative
 *
 * @author Lanscarlos
 * @since 2023-08-21 15:13
 */
class InventoryApplicative(source: Any) : AbstractApplicative<Inventory>(source) {

    override fun transfer(source: Any, def: Inventory?): Inventory? {
        return when (source) {
            is Inventory -> source
            is HumanEntity -> source.inventory
            is BukkitPlayer -> source.player.inventory
            is String -> {
                Bukkit.getPlayerExact(source)?.inventory
            }

            else -> def
        }
    }

    companion object {

        fun Any.applicativeInventory() = InventoryApplicative(this)
    }
}
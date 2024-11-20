package top.lanscarlos.vulpecula.applicative

import org.bukkit.Bukkit
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import taboolib.common.platform.ProxyPlayer

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.applicative
 *
 * @author Lanscarlos
 * @since 2023-08-21 15:13
 */
object InventoryApplicative : AbstractApplicative<Inventory>() {

    override fun transfer(instance: Any, def: Inventory?): Inventory? {
        return when (instance) {
            is Inventory -> instance
            is HumanEntity -> instance.inventory
            is ProxyPlayer -> instance.castSafely<Player>()?.inventory
            is String -> {
                Bukkit.getPlayerExact(instance)?.inventory
            }

            else -> def
        }
    }

    override fun readProperty(instance: Inventory, key: String): Any? {
        failedByGetPropertyNotSupported(instance, key)
    }

    override fun writeProperty(instance: Inventory, key: String, value: Any?) {
        failedBySetPropertyNotSupported(instance, key)
    }
}
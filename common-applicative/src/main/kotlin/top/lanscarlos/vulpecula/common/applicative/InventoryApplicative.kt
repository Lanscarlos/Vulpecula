package top.lanscarlos.vulpecula.common.applicative

import org.bukkit.Bukkit
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import taboolib.common.platform.ProxyPlayer

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.applicative
 *
 * @author Lanscarlos
 * @since 2023-08-21 15:13
 */
object InventoryApplicative : AbstractApplicative<Inventory>(Inventory::class.java) {

    override fun convert(instance: Any): Inventory {
        return when (instance) {
            is Inventory -> instance
            is HumanEntity -> instance.inventory
            is ProxyPlayer -> instance.cast<Player>().inventory
            is String -> Bukkit.getPlayerExact(instance)?.inventory ?: throw InvalidValueException(instance, Inventory::class.java)
            else -> throw UnsupportedTypeException(instance::class.java, Inventory::class.java)
        }
    }

    override fun readProperty(instance: Inventory, key: String): Any? {
        errorGetPropertyNotSupported(instance, key)
    }

    override fun writeProperty(instance: Inventory, key: String, value: Any?) {
        errorBySetPropertyNotSupported(instance, key)
    }
}
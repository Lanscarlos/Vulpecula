package top.lanscarlos.vulpecula.common.applicative

import org.bukkit.Bukkit
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import taboolib.common.platform.ProxyPlayer
import top.lanscarlos.vulpecula.common.applicative.exception.ValueConversionException
import top.lanscarlos.vulpecula.common.applicative.exception.TypeConversionException

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.applicative
 *
 * @author Lanscarlos
 * @since 2023-08-21 15:13
 */
object InventoryApplicative : AbstractApplicative<Inventory>(Inventory::class.java) {

    override fun convertOrThrow(instance: Any): Inventory {
        return when (instance) {
            is Inventory -> instance
            is HumanEntity -> instance.inventory
            is ProxyPlayer -> instance.cast<Player>().inventory
            is String -> Bukkit.getPlayerExact(instance)?.inventory ?: throw ValueConversionException(instance, Inventory::class.java)
            else -> throw TypeConversionException(instance::class.java, Inventory::class.java)
        }
    }

}
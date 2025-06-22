package top.lanscarlos.vulpecula.legacy.bacikal.action.item

import taboolib.platform.util.toBukkitLocation
import top.lanscarlos.vulpecula.legacy.utils.playerOrNull

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.item
 *
 * @author Lanscarlos
 * @since 2023-03-24 16:25
 */
object ActionItemDrop : ActionItem.Resolver {

    override val name: Array<String> = arrayOf("drop")

    override fun resolve(reader: ActionItem.Reader): ActionItem.Handler<out Any?> {
        return reader.handle {
            combine(
                source(),
                optional("at", "to", then = location(display = "drop location"))
            ) { item, location ->

                val loc = location?.toBukkitLocation() ?: this.playerOrNull()?.location?.toBukkitLocation()
                ?: error("No drop location selected.")

                loc.world?.dropItem(loc, item)
            }
        }
    }
}
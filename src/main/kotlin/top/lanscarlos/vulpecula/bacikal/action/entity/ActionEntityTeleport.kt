package top.lanscarlos.vulpecula.bacikal.action.entity

import org.bukkit.Location
import org.bukkit.event.player.PlayerTeleportEvent
import taboolib.platform.util.toBukkitLocation

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.entity
 *
 * @author Lanscarlos
 * @since 2022-11-17 23:26
 */
object ActionEntityTeleport : ActionEntity.Resolver {

    override val name: Array<String> = arrayOf("teleport", "tp")

    override fun resolve(reader: ActionEntity.Reader): ActionEntity.Handler<out Any?> {
        return reader.transfer {
            combine(
                source(),
                trim("to", "at", then = location()),
                optional("by", then = textOrNull())
            ) { entity, location, cause ->
                val loc = if (location.world == null) {
                    Location(entity.world, location.x, location.y, location.z)
                } else {
                    location.toBukkitLocation()
                }

                if (cause != null) {
                    val reason = PlayerTeleportEvent.TeleportCause.values().firstOrNull { it.name.equals(cause, true) }
                    if (reason != null) {
                        entity.teleport(loc, reason)
                        return@combine entity
                    }
                }

                entity.also { it.teleport(loc) }
            }
        }
    }
}
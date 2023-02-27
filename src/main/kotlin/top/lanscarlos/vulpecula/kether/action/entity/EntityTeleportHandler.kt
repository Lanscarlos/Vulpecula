package top.lanscarlos.vulpecula.kether.action.entity

import org.bukkit.event.player.PlayerTeleportEvent
import taboolib.common.util.Location
import taboolib.library.kether.QuestReader
import taboolib.platform.util.toBukkitLocation
import top.lanscarlos.vulpecula.kether.live.readLocation
import top.lanscarlos.vulpecula.kether.live.tryReadString
import top.lanscarlos.vulpecula.utils.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.entity
 *
 * @author Lanscarlos
 * @since 2022-11-17 23:26
 */
object EntityTeleportHandler : ActionEntity.Resolver {

    override val name: Array<String> = arrayOf("teleport", "tp")

    override fun resolve(reader: ActionEntity.Reader): ActionEntity.Handler<out Any?> {
        return reader.transfer {
            combine(
                source(),
                trim("to", "at", then = location()),
                optional("by", then = textOrNull())
            ) { entity, location, cause ->
                if (cause != null) {
                    val reason = PlayerTeleportEvent.TeleportCause.values().firstOrNull { it.name.equals(cause, true) }
                    if (reason != null) {
                        entity.teleport(location.toBukkitLocation(), reason)
                        return@combine entity
                    }
                }

                entity.also { it.teleport(location.toBukkitLocation()) }
            }
        }
    }
}
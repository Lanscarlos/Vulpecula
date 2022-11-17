package top.lanscarlos.vulpecula.kether.action.entity

import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.event.player.PlayerTeleportEvent
import taboolib.common.platform.ProxyPlayer
import taboolib.library.kether.QuestReader
import taboolib.module.kether.run
import taboolib.platform.util.toBukkitLocation
import top.lanscarlos.vulpecula.utils.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.entity
 *
 * @author Lanscarlos
 * @since 2022-11-17 23:26
 */
object EntityTeleportHandler : ActionEntity.Reader {

    override val name: Array<String> = arrayOf("teleport", "tp")

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionEntity.Handler {
        val source = if (isRoot) reader.readEntity() else null
        reader.hasNextToken("to")
        val destination = reader.readLocation()
        val reason = reader.tryReadString("by", "cause", "reason")

        return applyEntity(source) { entity ->
            val cause = reason?.getOrNull(this)?.let { reason ->
                PlayerTeleportEvent.TeleportCause.values().firstOrNull { reason.equals(it.name, true) }
            }

            destination.getOrNull(this)?.let {
                if (cause != null) {
                    entity.teleport(it.toBukkitLocation(), cause)
                } else {
                    entity.teleport(it.toBukkitLocation())
                }
            }

            entity
        }
    }
}
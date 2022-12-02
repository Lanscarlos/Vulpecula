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
object EntityTeleportHandler : ActionEntity.Reader {

    override val name: Array<String> = arrayOf("teleport", "tp")

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionEntity.Handler {
        val source = reader.source(isRoot)
        reader.hasNextToken("to")
        val destination = reader.readLocation()
        val reason = reader.tryReadString("by", "cause", "reason")

        return acceptTransferFuture(source) { entity ->
            listOf(
                destination.getOrNull(this),
                reason?.getOrNull(this)
            ).thenTake().thenApply { args ->
                val loc = (args[0] as? Location)?.toBukkitLocation() ?: error("No location selected.")
                val cause = args[1]?.toString()?.let { reason ->
                    PlayerTeleportEvent.TeleportCause.values().firstOrNull { it.name.equals(reason, true) }
                }

                if (cause != null) {
                    entity.teleport(loc, cause)
                } else {
                    entity.teleport(loc)
                }

                return@thenApply entity
            }
        }
    }
}
package top.lanscarlos.vulpecula.kether.action.target.selector

import org.bukkit.Bukkit
import taboolib.library.kether.QuestReader
import top.lanscarlos.vulpecula.kether.action.target.ActionTarget
import top.lanscarlos.vulpecula.utils.toBukkit
import top.lanscarlos.vulpecula.utils.hasNextToken
import top.lanscarlos.vulpecula.utils.playerOrNull

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.target.selector
 *
 * @author Lanscarlos
 * @since 2022-11-17 22:27
 */
@Deprecated("")
object PlayerOnServerSelector : ActionTarget.Reader {

    override val name: Array<String> = arrayOf("player-on-server", "players-on-server", "PlayerOnServer", "PlayersOnServer", "Server")

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionTarget.Handler {
        val includeSelf = reader.hasNextToken("-self")

        return handleNow { collection ->

            val self = if (!includeSelf) {
                this.playerOrNull()?.toBukkit()
            } else null

            Bukkit.getOnlinePlayers().forEach {
                if (self != null && it == self) return@forEach
                collection += it
            }

            collection
        }
    }
}
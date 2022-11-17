package top.lanscarlos.vulpecula.kether.action.target.selector

import org.bukkit.Bukkit
import taboolib.library.kether.QuestReader
import top.lanscarlos.vulpecula.kether.action.target.ActionTarget

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.target.selector
 *
 * @author Lanscarlos
 * @since 2022-11-17 22:27
 */
object PlayerOnServerSelector : ActionTarget.Reader {

    override val name: Array<String> = arrayOf("player-on-server", "PlayerOnServer", "Server")

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionTarget.Handler {
        return handle { collection ->
            Bukkit.getOnlinePlayers().forEach {
                collection += it
            }
            collection
        }
    }
}
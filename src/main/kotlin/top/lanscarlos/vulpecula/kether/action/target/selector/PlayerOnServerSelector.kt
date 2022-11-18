package top.lanscarlos.vulpecula.kether.action.target.selector

import org.bukkit.Bukkit
import taboolib.library.kether.QuestReader
import top.lanscarlos.vulpecula.kether.action.target.ActionTarget
import top.lanscarlos.vulpecula.utils.bukkit
import top.lanscarlos.vulpecula.utils.tryReadBoolean
import top.lanscarlos.vulpecula.utils.unsafePlayer

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
        val includeSelf = reader.tryReadBoolean("-self", "-include-self")

        return handle { collection ->

            val self = if (includeSelf?.get(this, false) == false) {
                this.unsafePlayer()?.bukkit()
            } else null

            Bukkit.getOnlinePlayers().forEach {
                if (self != null && it == self) return@forEach
                collection += it
            }

            collection
        }
    }
}
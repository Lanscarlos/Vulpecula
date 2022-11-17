package top.lanscarlos.vulpecula.kether.action.target.selector

import org.bukkit.Bukkit
import taboolib.library.kether.QuestReader
import top.lanscarlos.vulpecula.kether.action.target.ActionTarget
import top.lanscarlos.vulpecula.utils.readString

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.target.selector
 *
 * @author Lanscarlos
 * @since 2022-11-17 17:21
 */
object PlayerSelector : ActionTarget.Reader {

    override val name: Array<String> = arrayOf("player")

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionTarget.Handler {
        val name = reader.readString()

        return handle { collection ->
            val player = name.getOrNull(this)?.let {
                Bukkit.getPlayerExact(it)
            } ?: return@handle collection

            collection.also { collection.add(player) }
        }
    }
}
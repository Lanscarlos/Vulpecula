package top.lanscarlos.vulpecula.kether.action.target.selector

import taboolib.library.kether.QuestReader
import taboolib.module.kether.script
import taboolib.platform.type.BukkitPlayer
import top.lanscarlos.vulpecula.kether.action.target.ActionTarget

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.target.selector
 *
 * @author Lanscarlos
 * @since 2022-11-17 00:01
 */
@Deprecated("")
object SelfSelector : ActionTarget.Reader {

    override val name: Array<String> = arrayOf("self")

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionTarget.Handler {
        return handleNow { collection ->
            this.script().sender?.let {
                when (it) {
                    is BukkitPlayer -> collection += it.player
                    else -> collection += it
                }
            }
            collection
        }
    }
}
package top.lanscarlos.vulpecula.kether.action.target.selector

import taboolib.library.kether.QuestReader
import taboolib.module.kether.script
import top.lanscarlos.vulpecula.kether.action.target.ActionTarget

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.target.selector
 *
 * @author Lanscarlos
 * @since 2022-11-17 00:01
 */
object SelfSelector : ActionTarget.Reader {

    override val name: Array<String> = arrayOf("self")

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionTarget.Handler {
        return handleNow { collection ->
            this.script().sender?.let { collection += it }
            collection
        }
    }
}
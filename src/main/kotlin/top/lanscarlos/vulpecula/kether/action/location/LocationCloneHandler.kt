package top.lanscarlos.vulpecula.kether.action.location

import taboolib.library.kether.QuestReader
import top.lanscarlos.vulpecula.kether.live.readLocation

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.location
 *
 * @author Lanscarlos
 * @since 2022-11-26 11:21
 */
object LocationCloneHandler : ActionLocation.Reader {

    override val name: Array<String> = arrayOf("clone")

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionLocation.Handler {
        val source = if (isRoot) reader.readLocation() else null
        return acceptTransferNow(source, false) { location -> location.clone() }
    }
}
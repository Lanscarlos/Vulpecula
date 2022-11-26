package top.lanscarlos.vulpecula.kether.action.location

import taboolib.library.kether.QuestReader
import taboolib.platform.util.toBukkitLocation
import top.lanscarlos.vulpecula.utils.readLocation

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.location
 *
 * @author Lanscarlos
 * @since 2022-11-26 11:24
 */
object LocationBukkitHandler : ActionLocation.Reader {

    override val name: Array<String> = arrayOf("bukkit")

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionLocation.Handler {
        val source = if (isRoot) reader.readLocation() else null
        return acceptHandleNow(source) { location -> location.toBukkitLocation() }
    }
}
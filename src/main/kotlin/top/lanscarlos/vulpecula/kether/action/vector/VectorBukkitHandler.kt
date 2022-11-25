package top.lanscarlos.vulpecula.kether.action.vector

import taboolib.common.util.Vector
import taboolib.library.kether.QuestReader
import top.lanscarlos.vulpecula.kether.live.VectorLiveData
import top.lanscarlos.vulpecula.utils.nextBlock

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.vector
 *
 * @author Lanscarlos
 * @since 2022-11-14 17:49
 */
object VectorBukkitHandler : ActionVector.Reader {

    override val name: Array<String> = arrayOf("bukkit")

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionVector.Handler {
        val source = if (isRoot) VectorLiveData(reader.nextBlock()) else null

        return acceptHandleNow(source) { vector ->
            org.bukkit.util.Vector(vector.x, vector.y, vector.z)
        }
    }
}
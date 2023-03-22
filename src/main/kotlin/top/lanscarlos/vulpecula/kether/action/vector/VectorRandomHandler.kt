package top.lanscarlos.vulpecula.kether.action.vector

import taboolib.common.util.Vector
import taboolib.library.kether.QuestReader

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.vector
 *
 * @author Lanscarlos
 * @since 2022-11-14 18:57
 */
@Deprecated("")
object VectorRandomHandler : ActionVector.Reader {

    override val name: Array<String> = arrayOf("random")

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionVector.Handler {
        return transferNow { Vector.getRandom() }
    }
}
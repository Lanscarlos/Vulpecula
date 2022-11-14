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
 * @since 2022-11-14 18:58
 */
object VectorMidpointHandler : ActionVector.Reader {

    override val name: Array<String> = arrayOf("midpoint")

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionVector.Handler {
        val source = if (isRoot) VectorLiveData(reader.nextBlock()) else null
        val other = reader.expectVector("with", "using")
        val reproduced = reader.isReproduced()

        return acceptTransfer(source, false) { vector ->
            if (reproduced) {
                vector.getMidpoint(other.get(this, Vector()))
            } else {
                vector.midpoint(other.get(this, Vector()))
            }
        }
    }
}
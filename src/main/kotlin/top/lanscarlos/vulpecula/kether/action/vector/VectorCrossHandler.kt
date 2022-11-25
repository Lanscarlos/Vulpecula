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
 * @since 2022-11-14 17:50
 */
object VectorCrossHandler : ActionVector.Reader {

    override val name: Array<String> = arrayOf("cross")

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionVector.Handler {
        val source = if (isRoot) VectorLiveData(reader.nextBlock()) else null
        val other = reader.expectVector("with", "using")
        val reproduced = reader.isReproduced()

        return acceptTransferFuture(source, false) { vector ->
            other.get(this, Vector()).thenApply {
                if (reproduced) {
                    vector.getCrossProduct(it)
                } else {
                    vector.crossProduct(it)
                }
            }
        }
    }
}
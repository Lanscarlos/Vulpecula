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
 * @since 2022-11-14 18:53
 */
object VectorDistanceHandler : ActionVector.Reader {

    override val name: Array<String> = arrayOf(
        "distance", "dist", "distance-squared", "dist-sq"
    )

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionVector.Handler {
        val source = if (isRoot) VectorLiveData(reader.nextBlock()) else null
        val other = reader.expectVector("with", "using")

        return acceptHandler(source) { vector ->
            when (input) {
                "distance", "dist" -> vector.distance(other.get(this, Vector()))
                "distance-squared", "dist-sq" -> vector.distanceSquared(other.get(this, Vector()))
                else -> 0.0
            }
        }
    }
}
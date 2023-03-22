package top.lanscarlos.vulpecula.kether.action.vector

import taboolib.common.util.Vector
import taboolib.library.kether.QuestReader
import top.lanscarlos.vulpecula.utils.hasNextToken
import top.lanscarlos.vulpecula.kether.live.readLocation
import top.lanscarlos.vulpecula.kether.live.readVector

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.vector
 *
 * @author Lanscarlos
 * @since 2022-11-14 16:57
 */
@Deprecated("")
object VectorBuildHandler : ActionVector.Reader {

    override val name: Array<String> = arrayOf("build")

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionVector.Handler {
        if (reader.hasNextToken("by")) {
            val loc = reader.readLocation()

            return transferFuture {
                loc.getOrNull(this).thenApply { it?.toVector() ?: error("No location selected.") }
            }
        } else if (reader.hasNextToken("from")) {
            val loc = reader.readLocation()

            return transferFuture {
                loc.getOrNull(this).thenApply { it?.direction ?: error("No location selected.") }
            }
        } else {
            val vec = reader.readVector(true)
            return transferFuture {
                vec.get(this, Vector())
            }
        }
    }
}
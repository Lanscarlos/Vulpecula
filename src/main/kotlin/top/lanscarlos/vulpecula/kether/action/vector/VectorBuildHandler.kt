package top.lanscarlos.vulpecula.kether.action.vector

import taboolib.common.util.Location
import taboolib.common.util.Vector
import taboolib.library.kether.QuestReader
import top.lanscarlos.vulpecula.utils.hasNextToken
import top.lanscarlos.vulpecula.utils.readLocation
import top.lanscarlos.vulpecula.utils.readVector

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.vector
 *
 * @author Lanscarlos
 * @since 2022-11-14 16:57
 */
object VectorBuildHandler : ActionVector.Reader {

    override val name: Array<String> = arrayOf("build")

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionVector.Handler {
        val def = Location(null, 0.0, 0.0, 0.0)
        if (reader.hasNextToken("by")) {
            val loc = reader.readLocation()

            return transfer {
                loc.get(this, def).toVector()
            }
        } else if (reader.hasNextToken("from")) {
            val loc = reader.readLocation()

            return transfer {
                loc.get(this, def).direction
            }
        } else {
            val vec = reader.readVector(true)

            return transfer {
                vec.get(this, Vector())
            }
        }
    }
}
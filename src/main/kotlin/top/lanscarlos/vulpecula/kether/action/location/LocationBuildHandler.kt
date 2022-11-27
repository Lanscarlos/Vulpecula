package top.lanscarlos.vulpecula.kether.action.location

import taboolib.common.util.Location
import taboolib.common.util.Vector
import taboolib.library.kether.QuestReader
import top.lanscarlos.vulpecula.kether.live.DoubleLiveData
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.kether.live.StringLiveData
import top.lanscarlos.vulpecula.utils.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.location
 *
 * @author Lanscarlos
 * @since 2022-11-26 00:36
 */
object LocationBuildHandler : ActionLocation.Reader {

    override val name: Array<String> = arrayOf("build")

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionLocation.Handler {
        if (reader.hasNextToken("from")) {
            val vector = reader.readVector(false)
            reader.expect("with")
            val world = reader.readString()
            val extend = if (reader.hasNextToken("and")) {
                reader.readDouble() to reader.readDouble()
            } else {
                DoubleLiveData(0.0) to DoubleLiveData(0.0)
            }

            return transferFuture {
                listOf(
                    vector.getOrNull(this),
                    world.getOrNull(this),
                    extend.first.getOrNull(this),
                    extend.second.getOrNull(this)
                ).thenTake().thenApply { args ->
                    val vec = args[0] as? Vector ?: error("No vector selected.")
                    Location(
                        args[1]?.toString(),
                        vec.x, vec.y, vec.z,
                        args[2].coerceFloat(0f),
                        args[3].coerceFloat(0f)
                    )
                }
            }
        } else {
            val options = mutableMapOf<String, LiveData<*>>()

            options["x"] = reader.readDouble()
            options["y"] = reader.readDouble()
            options["z"] = reader.readDouble()

            while (reader.nextPeek().startsWith('-')) {
                when (val it = reader.nextToken().substring(1)) {
                    "world" -> options["world"] = StringLiveData(reader.nextBlock())
                    "yaw" -> options["yaw"] = reader.readDouble()
                    "pitch" -> options["pitch"] = reader.readDouble()
                    else -> error("Unknown argument \"$it\" at location build action.")
                }
            }

            return transferFuture {
                options.mapValues { it.value.getOrNull(this) }.thenTake().thenApply { args ->
                    Location(
                        args["world"]?.toString(),
                        args["x"].coerceDouble(0.0),
                        args["y"].coerceDouble(0.0),
                        args["z"].coerceDouble(0.0),
                        args["yaw"].coerceFloat(0f),
                        args["pitch"].coerceFloat(0f)
                    )
                }
            }
        }
    }

    fun readLegacy(reader: QuestReader): ActionLocation.Handler {
        val world = reader.nextToken()
        val x = reader.nextDouble()
        val y = reader.nextDouble()
        val z = reader.nextDouble()

        val extend = if (reader.hasNextToken("and")) {
            reader.nextDouble() to reader.nextDouble()
        } else null

        return transferNow {
            Location(
                world,
                x, y, z,
                extend?.first?.toFloat() ?: 0f,
                extend?.first?.toFloat() ?: 0f,
            )
        }
    }
}
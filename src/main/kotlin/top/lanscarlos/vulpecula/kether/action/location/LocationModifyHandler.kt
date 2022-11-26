package top.lanscarlos.vulpecula.kether.action.location

import taboolib.common.util.Location
import taboolib.library.kether.QuestReader
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.kether.live.StringLiveData
import top.lanscarlos.vulpecula.utils.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.location
 *
 * @author Lanscarlos
 * @since 2022-11-26 10:26
 */
object LocationModifyHandler : ActionLocation.Reader {

    override val name: Array<String> = arrayOf(
        "modify", "set",
        "add",
        "subtract", "sub",
        "multiply", "mul",
        "divide", "div"
    )

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionLocation.Handler {
        val source = if (isRoot) reader.readLocation() else null
        return when (input) {
            "modify", "set" -> modify(reader, source)
            "add",
            "subtract", "sub",
            "multiply", "mul",
            "divide", "div" -> operation(reader, source, input)
            else -> {
                acceptTransferNow(source, false) { location -> location }
            }
        }
    }

    private fun modify(reader: QuestReader, source: LiveData<Location>?): ActionLocation.Handler {
        val options = mutableMapOf<String, LiveData<*>>()

        // 判断下一个 token 是否为数字（包括负号），或不以 - 开头
        if (reader.nextPeek().toDoubleOrNull() != null || !reader.nextPeek().startsWith('-')) {
            options["x"] = reader.readDouble()
            options["y"] = reader.readDouble()
            options["z"] = reader.readDouble()
        }

        while (reader.nextPeek().startsWith('-')) {
            when (val it = reader.nextToken().substring(1)) {
                "x" -> options["x"] = reader.readDouble()
                "y" -> options["y"] = reader.readDouble()
                "z" -> options["z"] = reader.readDouble()
                "world" -> options["world"] = StringLiveData(reader.nextBlock())
                "yaw" -> options["yaw"] = reader.readDouble()
                "pitch" -> options["pitch"] = reader.readDouble()
                else -> error("Unknown argument \"$it\" at location modify action.")
            }
        }
        val reproduced = this.isReproduced(reader)

        return acceptTransferFuture(source, reproduced) { location ->
            options.mapValues { it.value.getOrNull(this) }.thenTake().thenApply { args ->
                for (option in args) {
                    when (option.key) {
                        "x" -> location.x = option.value.toDouble(location.x)
                        "y" -> location.y = option.value.toDouble(location.y)
                        "z" -> location.z = option.value.toDouble(location.z)
                        "yaw" -> location.yaw = option.value.toFloat(location.yaw)
                        "pitch" -> location.pitch = option.value.toFloat(location.pitch)
                    }
                }

                // 修改世界名
                if ("world" in args) {
                    val world = args["world"]?.toString()
                    return@thenApply Location(world, location.x, location.y, location.z, location.pitch, location.yaw)
                } else {
                    return@thenApply location
                }
            }
        }
    }

    private fun operation(reader: QuestReader, source: LiveData<Location>?, input: String): ActionLocation.Handler {
        if (reader.hasNextToken("with")) {
            val other = reader.readLocation()
            val reproduced = this.isReproduced(reader)

            return acceptTransferFuture(source, reproduced) { location ->
                other.getOrNull(this).thenApply {
                    if (it == null) return@thenApply location
                    when (input) {
                        "add" -> location.add(it)
                        "subtract", "sub" -> location.subtract(it)
                        "multiply", "mul" -> {
                            location.also { loc ->
                                loc.x *= it.x
                                loc.y *= it.y
                                loc.z *= it.z
                            }
                        }
                        "divide", "div" -> {
                            location.also { loc ->
                                loc.x /= it.x
                                loc.y /= it.y
                                loc.z /= it.z
                            }
                        }
                        else -> location
                    }
                }
            }
        } else {
            val options = mutableMapOf<String, LiveData<*>>()
            if (!reader.nextPeek().startsWith('-')) {
                options["x"] = reader.readDouble()
                options["y"] = reader.readDouble()
                options["z"] = reader.readDouble()
            }
            while (reader.nextPeek().toDoubleOrNull() != null || reader.nextPeek().startsWith('-')) {
                when (val it = reader.nextToken().substring(1)) {
                    "x" -> options["x"] = reader.readDouble()
                    "y" -> options["y"] = reader.readDouble()
                    "z" -> options["z"] = reader.readDouble()
                    "yaw" -> options["yaw"] = reader.readDouble()
                    "pitch" -> options["pitch"] = reader.readDouble()
                    else -> error("Unknown argument \"$it\" at location add action.")
                }
            }
            val reproduced = this.isReproduced(reader)

            return acceptTransferFuture(source, reproduced) { location ->
                options.mapValues { it.value.getOrNull(this) }.thenTake().thenApply { args ->
                    for (option in args) {
                        when (option.key) {
                            "x" -> location.x = location.x.operation(input, option.value)
                            "y" -> location.y = location.y.operation(input, option.value)
                            "z" -> location.z = location.z.operation(input, option.value)
                            "yaw" -> location.yaw = location.yaw.operation(input, option.value)
                            "pitch" -> location.pitch = location.pitch.operation(input, option.value)
                        }
                    }

                    return@thenApply location
                }
            }
        }
    }

    private fun Float.operation(input: String, value: Any?): Float {
        return when (input) {
            "add" -> this + value.toFloat(0f)
            "subtract", "sub" -> this - value.toFloat(0f)
            "multiply", "mul" -> this * value.toFloat(1f)
            "divide", "div" -> this / value.toFloat(1f)
            else -> this
        }
    }

    private fun Double.operation(input: String, value: Any?): Double {
        return when (input) {
            "add" -> this + value.toDouble(0.0)
            "subtract", "sub" -> this - value.toDouble(0.0)
            "multiply", "mul" -> this * value.toDouble(1.0)
            "divide", "div" -> this / value.toDouble(1.0)
            else -> this
        }
    }
}
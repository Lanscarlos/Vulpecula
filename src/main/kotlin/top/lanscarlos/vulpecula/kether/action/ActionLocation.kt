package top.lanscarlos.vulpecula.kether.action

import taboolib.common.util.Location
import taboolib.common.util.Vector
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.scriptParser
import taboolib.platform.util.toBukkitLocation
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.kether.live.*
import top.lanscarlos.vulpecula.utils.*
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action
 *
 * @author Lanscarlos
 * @since 2022-11-11 13:11
 */
class ActionLocation : ScriptAction<Any>() {

    interface Handler {
        fun handle(frame: ScriptFrame, previous: Location?): Any
    }

    interface TransferHandler : Handler {
        override fun handle(frame: ScriptFrame, previous: Location?): Location
    }

    private val handlers = mutableListOf<Handler>()

    override fun run(frame: ScriptFrame): CompletableFuture<Any> {
        var previous: Location? = null
        for (handler in handlers) {
            if (handler is TransferHandler) {
                previous = handler.handle(frame, previous)
            } else {
                return CompletableFuture.completedFuture(
                    handler.handle(frame, previous)
                )
            }
        }
        return CompletableFuture.completedFuture(previous)
    }

    companion object {

        val def: Location get() = Location("world", 0.0, 0.0, 0.0)

        /**
         *
         * 构建坐标
         * loc build x y z -yaw &yaw
         * loc build x y z -pitch(p) &pitch
         * loc build x y z -world(w) &world
         * loc build from &vec with &world [and &yaw &pitch]
         *
         * 修改坐标
         * loc modify &loc x y z
         * loc set &loc x y z
         * loc set &loc -yaw(y) &yaw
         * loc set &loc -pitch(p) &pitch
         * loc set &loc -world(w) &world
         * loc set &loc ~ 3 ~
         *
         * 增加对应数值.
         * loc add &loc x y z
         * loc add &loc -yaw(y) &yaw
         * loc add &loc with &other
         *
         * 减少对应数值.
         * loc sub &loc x y z
         * loc sub &loc by &other
         *
         * 坐标数乘, 将所有坐标轴上扩展某个倍数.
         * loc multiply &loc with/to &amount
         * loc mul &loc with/to &amount
         * loc mul &loc x y z
         *
         * 克隆坐标.
         * loc clone &loc
         * &loc[clone]
         *
         * 获取 Bukkit 坐标
         * loc bukkit &loc
         * &loc[bukkit]
         *
         * 获取本位置与与另一个位置之间的距离.
         * loc distance &loc to &other
         * loc dist &loc to &other
         *
         * 获取本位置与与另一个位置之间的距离的平方.
         * loc distance-Squared &loc to &other
         * loc dist-sq &loc to &other
         * */
        @VulKetherParser(
            id = "location",
            name = ["location*", "loc"],
            override = ["location"]
        )
        fun parser() = scriptParser { reader ->
            val action = ActionLocation()
            do {
                val isRoot = action.handlers.isEmpty()
                reader.mark()
                val it = reader.nextToken()
                action.handlers += when (it) {
                    "build" -> build(reader)
                    "modify", "set" -> modify(isRoot, reader)
                    "add" -> add(isRoot, reader)
                    "sub" -> sub(isRoot, reader)
                    "mul" -> mul(isRoot, reader)
                    "clone" -> clone(isRoot, reader)
                    "bukkit" -> bukkit(isRoot, reader)
                    "distance", "dist" -> distance(isRoot, reader)
                    "dist-sq" -> distanceSquared(isRoot, reader)
                    else -> {
                        // 兼容 TabooLib 原生 location 语句的构建坐标功能
                        reader.reset()
                        val world = StringLiveData(reader.nextBlock())
                        val x = reader.readDouble()
                        val y = reader.readDouble()
                        val z = reader.readDouble()
                        val extend = if (reader.hasNextToken("and")) {
                            reader.readDouble() to reader.readDouble()
                        } else {
                            DoubleLiveData(0.0) to DoubleLiveData(0.0)
                        }
                        transfer {
                            Location(
                                world.get(this, this.unsafePlayer()?.world ?: "world"),
                                x.get(this, 0.0),
                                y.get(this, 0.0),
                                z.get(this, 0.0),
                                extend.first.get(this, 0.0).toFloat(),
                                extend.second.get(this, 0.0).toFloat(),
                            )
                        }
                    }
//                    else -> error("Unknown argument \"$it\" at vector action.")
                }
                if (action.handlers.lastOrNull() !is TransferHandler) {
                    if (reader.hasNextToken(">>")) {
                        error("Cannot use \">> ${reader.nextPeek()}\", previous action \"$it\" has closed the pipeline.")
                    }
                    break
                }
            } while (reader.hasNextToken(">>"))

            return@scriptParser action
        }

        fun build(reader: QuestReader): Handler {
            return if (reader.hasNextToken("from")) {
                val vector = VectorLiveData(reader.nextBlock())
                reader.expect("with")
                val world = StringLiveData(reader.nextBlock())
                val extend = if (reader.hasNextToken("and")) {
                    reader.readDouble() to reader.readDouble()
                } else {
                    DoubleLiveData(0.0) to DoubleLiveData(0.0)
                }
                transfer {
                    val vec = vector.get(this, Vector())
                    Location(
                        world.get(this, this.unsafePlayer()?.world ?: "world"),
                        vec.x, vec.y, vec.z,
                        extend.first.get(this, 0.0).toFloat(),
                        extend.second.get(this, 0.0).toFloat(),
                    )
                }
            } else {
                val x = reader.readDouble()
                val y = reader.readDouble()
                val z = reader.readDouble()
                val options = mutableMapOf<String, LiveData<*>>()
                while (reader.nextPeek().startsWith('-')) {
                    when (val it = reader.nextToken().substring(1)) {
                        "world" -> options["world"] = StringLiveData(reader.nextBlock())
                        "yaw" -> options["yaw"] = reader.readDouble()
                        "pitch" -> options["pitch"] = reader.readDouble()
                        else -> error("Unknown argument \"$it\" at location build action.")
                    }
                }
                transfer {
                    Location(
                        options["world"].getValue(this, this.unsafePlayer()?.world ?: "world"),
                        x.get(this, 0.0),
                        y.get(this, 0.0),
                        z.get(this, 0.0),
                        options["yaw"].getValue(this, 0.0).toFloat(),
                        options["pitch"].getValue(this, 0.0).toFloat()
                    )
                }
            }
        }

        fun modify(isRoot: Boolean, reader: QuestReader): Handler {
            val location = if (isRoot) LocationLiveData(reader.nextBlock()) else null
            val options = mutableMapOf<String, LiveData<*>>()
            if (!reader.nextPeek().startsWith('-')) {
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

            return transfer { previous ->
                val loc = previous ?: location?.get(this, def) ?: error("No location selected.")
                loc.let {
                    if (reproduced) it.clone() else it
                }.also {
                    for (option in options) {
                        when (option.key) {
                            "x" -> it.x = option.value.getValue(this, it.x)
                            "y" -> it.y = option.value.getValue(this, it.y)
                            "z" -> it.z = option.value.getValue(this, it.z)
                            "yaw" -> it.yaw = option.value.getValue(this, it.yaw.toDouble()).toFloat()
                            "pitch" -> it.pitch = option.value.getValue(this, it.pitch.toDouble()).toFloat()
                        }
                    }
                }.let {
                    // 修改世界名
                    if ("world" in options) {
                        val world = options["world"].getValue(this, it.world)
                        Location(world, it.x, it.y, it.z, it.pitch, it.yaw)
                    } else it
                }
            }
        }

        fun add(isRoot: Boolean, reader: QuestReader): Handler {
            val location = if (isRoot) LocationLiveData(reader.nextBlock()) else null
            return if (reader.hasNextToken("with")) {
                val other = LocationLiveData(reader.nextBlock())
                val reproduced = this.isReproduced(reader)

                transfer { previous ->
                    val loc = previous ?: location?.get(this, def) ?: error("No location selected.")
                    loc.let {
                        if (reproduced) it.clone() else it
                    }.add(other.get(this, def))
                }
            } else {
                val options = mutableMapOf<String, LiveData<*>>()
                if (!reader.nextPeek().startsWith('-')) {
                    options["x"] = reader.readDouble()
                    options["y"] = reader.readDouble()
                    options["z"] = reader.readDouble()
                }
                while (reader.nextPeek().startsWith('-')) {
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

                transfer { previous ->
                    val loc = previous ?: location?.get(this, def) ?: error("No location selected.")
                    loc.let {
                        if (reproduced) it.clone() else it
                    }.also {
                        for (option in options) {
                            when (option.key) {
                                "x" -> it.x += option.value.getValue(this, 0.0)
                                "y" -> it.y += option.value.getValue(this, 0.0)
                                "z" -> it.z += option.value.getValue(this, 0.0)
                                "yaw" -> it.yaw += option.value.getValue(this, 0.0).toFloat()
                                "pitch" -> it.pitch += option.value.getValue(this, 0.0).toFloat()
                            }
                        }
                    }
                }
            }
        }

        fun sub(isRoot: Boolean, reader: QuestReader): Handler {
            val location = if (isRoot) LocationLiveData(reader.nextBlock()) else null
            return if (reader.hasNextToken("by")) {
                val other = LocationLiveData(reader.nextBlock())
                val reproduced = this.isReproduced(reader)

                transfer { previous ->
                    val loc = previous ?: location?.get(this, def) ?: error("No location selected.")
                    loc.let {
                        if (reproduced) it.clone() else it
                    }.subtract(other.get(this, def))
                }
            } else {
                val options = mutableMapOf<String, LiveData<*>>()
                if (!reader.nextPeek().startsWith('-')) {
                    options["x"] = reader.readDouble()
                    options["y"] = reader.readDouble()
                    options["z"] = reader.readDouble()
                }
                while (reader.nextPeek().startsWith('-')) {
                    when (val it = reader.nextToken().substring(1)) {
                        "x" -> options["x"] = reader.readDouble()
                        "y" -> options["y"] = reader.readDouble()
                        "z" -> options["z"] = reader.readDouble()
                        "yaw" -> options["yaw"] = reader.readDouble()
                        "pitch" -> options["pitch"] = reader.readDouble()
                        else -> error("Unknown argument \"$it\" at location subtract action.")
                    }
                }
                val reproduced = this.isReproduced(reader)

                transfer { previous ->
                    val loc = previous ?: location?.get(this, def) ?: error("No location selected.")
                    loc.let {
                        if (reproduced) it.clone() else it
                    }.also {
                        for (option in options) {
                            when (option.key) {
                                "x" -> it.x -= option.value.getValue(this, 0.0)
                                "y" -> it.y -= option.value.getValue(this, 0.0)
                                "z" -> it.z -= option.value.getValue(this, 0.0)
                                "yaw" -> it.yaw -= option.value.getValue(this, 0.0).toFloat()
                                "pitch" -> it.pitch -= option.value.getValue(this, 0.0).toFloat()
                            }
                        }
                    }
                }
            }
        }

        fun mul(isRoot: Boolean, reader: QuestReader): Handler {
            val location = if (isRoot) LocationLiveData(reader.nextBlock()) else null
            return if (reader.hasNextToken("with", "to")) {
                val amount = reader.readDouble()
                val reproduced = this.isReproduced(reader)

                transfer { previous ->
                    val loc = previous ?: location?.get(this, def) ?: error("No location selected.")
                    loc.let {
                        if (reproduced) it.clone() else it
                    }.multiply(amount.get(this, 1.0))
                }
            } else {
                val options = mutableMapOf<String, LiveData<*>>()
                if (!reader.nextPeek().startsWith('-')) {
                    options["x"] = reader.readDouble()
                    options["y"] = reader.readDouble()
                    options["z"] = reader.readDouble()
                }
                while (reader.nextPeek().startsWith('-')) {
                    when (val it = reader.nextToken().substring(1)) {
                        "x" -> options["x"] = reader.readDouble()
                        "y" -> options["y"] = reader.readDouble()
                        "z" -> options["z"] = reader.readDouble()
                        "yaw" -> options["yaw"] = reader.readDouble()
                        "pitch" -> options["pitch"] = reader.readDouble()
                        else -> error("Unknown argument \"$it\" at location multiply action.")
                    }
                }
                val reproduced = this.isReproduced(reader)

                transfer { previous ->
                    val loc = previous ?: location?.get(this, def) ?: error("No location selected.")
                    loc.let {
                        if (reproduced) it.clone() else it
                    }.also {
                        for (option in options) {
                            when (option.key) {
                                "x" -> it.x *= option.value.getValue(this, 1.0)
                                "y" -> it.y *= option.value.getValue(this, 1.0)
                                "z" -> it.z *= option.value.getValue(this, 1.0)
                                "yaw" -> it.yaw *= option.value.getValue(this, 1.0).toFloat()
                                "pitch" -> it.pitch *= option.value.getValue(this, 1.0).toFloat()
                            }
                        }
                    }
                }
            }
        }

        fun clone(isRoot: Boolean, reader: QuestReader): Handler {
            val location = if (isRoot) LocationLiveData(reader.nextBlock()) else null
            return transfer { previous ->
                val loc = previous ?: location?.get(this, def) ?: error("No location selected.")
                loc.clone()
            }
        }

        fun bukkit(isRoot: Boolean, reader: QuestReader): Handler {
            val location = if (isRoot) LocationLiveData(reader.nextBlock()) else null
            return handle { previous ->
                val loc = previous ?: location?.get(this, def) ?: error("No location selected.")
                loc.toBukkitLocation()
            }
        }

        fun distance(isRoot: Boolean, reader: QuestReader): Handler {
            val location = if (isRoot) LocationLiveData(reader.nextBlock()) else null
            val other = this.expectVector(reader, "with", "using")

            return handle { previous ->
                val loc = previous ?: location?.get(this, def) ?: error("No location selected.")
                loc.distance(other.get(this, loc))
            }
        }

        fun distanceSquared(isRoot: Boolean, reader: QuestReader): Handler {
            val location = if (isRoot) LocationLiveData(reader.nextBlock()) else null
            val other = this.expectVector(reader, "with", "using")

            return handle { previous ->
                val loc = previous ?: location?.get(this, def) ?: error("No location selected.")
                loc.distanceSquared(other.get(this, loc))
            }
        }

        private fun handle(func: ScriptFrame.(previous: Location?) -> Any): Handler {
            return object : Handler {
                override fun handle(frame: ScriptFrame, previous: Location?): Any {
                    return func(frame, previous)
                }
            }
        }

        private fun transfer(func: ScriptFrame.(previous: Location?) -> Location): Handler {
            return object : TransferHandler {
                override fun handle(frame: ScriptFrame, previous: Location?): Location {
                    return func(frame, previous)
                }
            }
        }

        /**
         * 读取期望 Location 数据
         * 主要用于运算
         *
         * @param expect 期望前缀，若找不到则使用 other 来构建 Location
         * @param other 用来标识构建 Location 的前缀
         * */
        private fun expectVector(reader: QuestReader, expect: String, other: String): LiveData<Location> {
            return if (reader.hasNextToken(expect)) {
                LocationLiveData(reader.nextBlock())
            } else {
                reader.expect(other)
                val x = reader.readDouble()
                val y = reader.readDouble()
                val z = reader.readDouble()
                LocationLiveData(Triple(x, y, z))
            }
        }

        private fun isReproduced(reader: QuestReader): Boolean {
            return !reader.hasNextToken("not-reproduced", "not-rep", "not-clone", "-n")
        }
    }
}
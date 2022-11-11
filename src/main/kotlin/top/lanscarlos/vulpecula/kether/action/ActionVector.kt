package top.lanscarlos.vulpecula.kether.action

import taboolib.common.util.Location
import taboolib.common.util.Vector
import taboolib.library.kether.QuestReader
import taboolib.module.effect.VectorUtils
import taboolib.module.kether.*
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.kether.live.VectorLiveData
import top.lanscarlos.vulpecula.utils.*
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action
 *
 * @author Lanscarlos
 * @since 2022-11-11 13:33
 */
class ActionVector : ScriptAction<Any?>() {

    interface Handler {
        fun handle(frame: ScriptFrame, previous: Vector?): Any
    }

    interface TransferHandler : Handler {
        override fun handle(frame: ScriptFrame, previous: Vector?): Vector
    }

    private val handlers = mutableListOf<Handler>()

    override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
        var previous: Vector? = null
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

        /**
         *
         * 构建向量
         * vec build x y z
         * vec build &x &y &z
         * 根据坐标构建向量
         * vec build by &loc
         * 根据坐标方向构建单位向量
         * vec build from &loc
         *
         * 修改向量
         * vec modify &vec x y z
         * vec set &vec x y z
         * vec set &vec ~ 0 &z
         *
         * 添加/减少
         * vec add/sub &vec x y z
         * vec add/sub &vec to &amount
         * vec add &vec with &other
         * vec sub &vec by &other
         *
         * 向量的数乘,将向量在所有轴上扩展某个倍数.
         * vec mul &vec x y z
         * vec mul &vec to &amount
         * vec mul &vec with &other
         *
         * 将本向量的坐标除以另一个向量的坐标.
         * vec div &vec x y z
         * vec div &vec to &amount
         * vec div &vec by &other
         *
         * 克隆向量
         * vec clone &vec
         * &vec[clone]
         *
         * 转换为 Bukkit 向量
         * vec bukkit &vec
         * &vec[bukkit]
         *
         * 获取本向量与另一个向量的夹角,用弧度表示.
         * vec angle &vec with &other
         *
         * 返回一个新的向量,其坐标为本向量与另一个向量的叉积.
         * vec crossProduct &vec with &other
         * vec cross &vec with &other
         *
         * 获取本向量与与另一个向量之间的距离.
         * vec distance &vec with &other
         * vec dist &vec with &other
         *
         * 获取本向量与与另一个向量之间的距离的平方.
         * vec distanceSquared &vec with &other
         * vec dist-sq &vec with &other
         *
         * 计算本向量与另一个向量的点积,定义为x1*x2+y1*y2+z1*z2.
         * vec dot &vec by &other
         *
         * 获取一个随机向量,其坐标值均为0到1之间(不含1).
         * vec random
         *
         * 获取两个向量连线的中点向量的坐标.
         * vec midpoint &vec with &other
         *
         * 获取向量的模值,定义为 sqrt(x^2+y^2+z^2).
         * vec length &vec
         *
         * 获取向量的模的平方.
         * vec length-sq &vec
         *
         * 将本向量转化为单位向量(模为1的向量).
         * vec normalize &vec
         *
         * 向量旋转
         * vec rotate x &vec &angle
         * vec rotate y &vec &angle
         * vec rotate z &vec &angle
         * vec rotate yaw-pitch &vec &yaw &pitch
         * vec rotate axis &vec by &axis &angle
         * vec rotate non-unit-axis &vec by &axis &angle
         * */
        @VulKetherParser(
            id = "vector",
            name = ["vector", "vec"]
        )
        fun parser() = scriptParser { reader ->
            val action = ActionVector()
            do {
                val isRoot = action.handlers.isEmpty()
                action.handlers += when (val it = reader.nextToken()) {
                    "build" -> build(reader)
                    "modify", "set" -> modify(isRoot, reader)
                    "add" -> add(isRoot, reader)
                    "sub" -> sub(isRoot, reader)
                    "mul" -> mul(isRoot, reader)
                    "div" -> div(isRoot, reader)
                    "clone" -> clone(isRoot, reader)
                    "bukkit" -> bukkit(isRoot, reader)
                    "angle" -> angle(isRoot, reader)
                    "cross" -> cross(isRoot, reader)
                    "distance", "dist" -> distance(isRoot, reader)
                    "dist-sq" -> distanceSquared(isRoot, reader)
                    "dot" -> dot(isRoot, reader)
                    "random" -> random()
                    "midpoint" -> midpoint(isRoot, reader)
                    "length" -> length(isRoot, reader)
                    "length-sq" -> lengthSquared(isRoot, reader)
                    "normalize" -> normalize(isRoot, reader)
                    "rotate" -> rotate(isRoot, reader)
                    else -> error("Unknown argument \"$it\" at vector action.")
                }
                if (action.handlers.lastOrNull() !is TransferHandler) {
                    if (reader.hasNextToken(">>")) {
                        error("Cannot use \">> ${reader.nextPeek()}\", previous action has closed the pipeline.")
                    }
                    break
                }
            } while (reader.hasNextToken(">>"))

            return@scriptParser action
        }

        fun build(reader: QuestReader): Handler {
            val def = Location("world", 0.0, 0.0, 0.0)
            return if (reader.hasNextToken("by")) {
                val loc = reader.readLocation()
                transfer {
                    loc.get(this, def).toVector()
                }
            } else if (reader.hasNextToken("from")) {
                val loc = reader.readLocation()
                transfer {
                    loc.get(this, def).direction
                }
            } else {
                val vec = VectorLiveData(Triple(
                    reader.readDouble(),
                    reader.readDouble(),
                    reader.readDouble()
                ))
                transfer {
                    vec.get(this, Vector())
                }
            }
        }

        fun modify(isRoot: Boolean, reader: QuestReader): Handler {
            val vector = if (isRoot) VectorLiveData(reader.nextBlock()) else null
            val liveData = if (reader.hasNextToken("to")) {
                val amount = reader.readDouble()
                VectorLiveData(Triple(amount, amount, amount))
            } else {
                val x = reader.readDouble()
                val y = reader.readDouble()
                val z = reader.readDouble()
                VectorLiveData(Triple(x, y, z))
            }
            val reproduced = this.isReproduced(reader)

            return transfer { previous ->
                val vec = previous ?: vector?.get(this, Vector()) ?: error("No vector selected.")
                val other = liveData.get(this, vec)
                vec.let {
                    if (reproduced) it.clone() else it
                }.also {
                    it.x = other.x
                    it.y = other.y
                    it.z = other.z
                }
            }
        }

        fun add(isRoot: Boolean, reader: QuestReader): Handler {
            val vector = if (isRoot) VectorLiveData(reader.nextBlock()) else null
            val other = this.readVector(reader, "with")
            val reproduced = this.isReproduced(reader)
            return transfer { previous ->
                val vec = previous ?: vector?.get(this, Vector()) ?: error("No vector selected.")
                vec.let {
                    if (reproduced) it.clone() else it
                }.also {
                    it.add(other.get(this, Vector()))
                }
            }
        }

        fun sub(isRoot: Boolean, reader: QuestReader): Handler {
            val vector = if (isRoot) VectorLiveData(reader.nextBlock()) else null
            val other = this.readVector(reader, "by")
            val reproduced = this.isReproduced(reader)
            return transfer { previous ->
                val vec = previous ?: vector?.get(this, Vector()) ?: error("No vector selected.")
                vec.let {
                    if (reproduced) it.clone() else it
                }.also {
                    it.subtract(other.get(this, Vector()))
                }
            }
        }

        fun mul(isRoot: Boolean, reader: QuestReader): Handler {
            val vector = if (isRoot) VectorLiveData(reader.nextBlock()) else null
            val other = this.readVector(reader, "with")
            val reproduced = this.isReproduced(reader)
            return transfer { previous ->
                val vec = previous ?: vector?.get(this, Vector()) ?: error("No vector selected.")
                vec.let {
                    if (reproduced) it.clone() else it
                }.also {
                    it.multiply(other.get(this, Vector()))
                }
            }
        }

        fun div(isRoot: Boolean, reader: QuestReader): Handler {
            val vector = if (isRoot) VectorLiveData(reader.nextBlock()) else null
            val other = this.readVector(reader, "by")
            val reproduced = this.isReproduced(reader)
            return transfer { previous ->
                val vec = previous ?: vector?.get(this, Vector()) ?: error("No vector selected.")
                vec.let {
                    if (reproduced) it.clone() else it
                }.also {
                    it.divide(other.get(this, Vector()))
                }
            }
        }

        fun clone(isRoot: Boolean, reader: QuestReader): Handler {
            val vector = if (isRoot) VectorLiveData(reader.nextBlock()) else null
            return transfer { previous ->
                val vec = previous ?: vector?.get(this, Vector()) ?: error("No vector selected.")
                vec.clone()
            }
        }

        fun bukkit(isRoot: Boolean, reader: QuestReader): Handler {
            val vector = if (isRoot) VectorLiveData(reader.nextBlock()) else null
            return handle { previous ->
                val vec = previous ?: vector?.get(this, Vector()) ?: error("No vector selected.")
                org.bukkit.util.Vector(vec.x, vec.y, vec.z)
            }
        }

        fun angle(isRoot: Boolean, reader: QuestReader): Handler {
            val vector = if (isRoot) VectorLiveData(reader.nextBlock()) else null
            val other = this.expectVector(reader, "with", "using")
            return handle { previous ->
                val vec = previous ?: vector?.get(this, Vector()) ?: error("No vector selected.")
                vec.angle(other.get(this, Vector()))
            }
        }

        fun cross(isRoot: Boolean, reader: QuestReader): Handler {
            val vector = if (isRoot) VectorLiveData(reader.nextBlock()) else null
            val other = this.expectVector(reader, "with", "using")
            val reproduced = this.isReproduced(reader)
            return transfer { previous ->
                val vec = previous ?: vector?.get(this, Vector()) ?: error("No vector selected.")
                if (reproduced) {
                    vec.getCrossProduct(other.get(this, Vector()))
                } else {
                    vec.crossProduct(other.get(this, Vector()))
                }
            }
        }

        fun distance(isRoot: Boolean, reader: QuestReader): Handler {
            val vector = if (isRoot) VectorLiveData(reader.nextBlock()) else null
            val other = this.expectVector(reader, "with", "using")
            return handle { previous ->
                val vec = previous ?: vector?.get(this, Vector()) ?: error("No vector selected.")
                vec.distance(other.get(this, Vector()))
            }
        }

        fun distanceSquared(isRoot: Boolean, reader: QuestReader): Handler {
            val vector = if (isRoot) VectorLiveData(reader.nextBlock()) else null
            val other = this.expectVector(reader, "with", "using")
            return handle { previous ->
                val vec = previous ?: vector?.get(this, Vector()) ?: error("No vector selected.")
                vec.distanceSquared(other.get(this, Vector()))
            }
        }

        fun dot(isRoot: Boolean, reader: QuestReader): Handler {
            val vector = if (isRoot) VectorLiveData(reader.nextBlock()) else null
            val other = this.expectVector(reader, "with", "using")
            return handle { previous ->
                val vec = previous ?: vector?.get(this, Vector()) ?: error("No vector selected.")
                vec.dot(other.get(this, Vector()))
            }
        }

        fun random(): Handler {
            return transfer { Vector.getRandom() }
        }

        fun midpoint(isRoot: Boolean, reader: QuestReader): Handler {
            val vector = if (isRoot) VectorLiveData(reader.nextBlock()) else null
            val other = this.expectVector(reader, "with", "using")
            return transfer { previous ->
                val vec = previous ?: vector?.get(this, Vector()) ?: error("No vector selected.")
                vec.midpoint(other.get(this, Vector()))
            }
        }

        fun length(isRoot: Boolean, reader: QuestReader): Handler {
            val vector = if (isRoot) VectorLiveData(reader.nextBlock()) else null
            return handle { previous ->
                val vec = previous ?: vector?.get(this, Vector()) ?: error("No vector selected.")
                vec.length()
            }
        }

        fun lengthSquared(isRoot: Boolean, reader: QuestReader): Handler {
            val vector = if (isRoot) VectorLiveData(reader.nextBlock()) else null
            return handle { previous ->
                val vec = previous ?: vector?.get(this, Vector()) ?: error("No vector selected.")
                vec.lengthSquared()
            }
        }

        fun normalize(isRoot: Boolean, reader: QuestReader): Handler {
            val vector = if (isRoot) VectorLiveData(reader.nextBlock()) else null
            val reproduced = this.isReproduced(reader)
            return transfer { previous ->
                val vec = previous ?: vector?.get(this, Vector()) ?: error("No vector selected.")
                vec.let {
                    if (reproduced) it.clone() else it
                }.normalize()
            }
        }

        fun rotate(isRoot: Boolean, reader: QuestReader): Handler {
            val vector = if (isRoot) VectorLiveData(reader.nextBlock()) else null
            return when (val it = reader.expects(
                "x", "y", "z", "axis",
                "non-unit-axis"
            )) {
                "x" -> rotateAroundX(vector, reader)
                "y" -> rotateAroundY(vector, reader)
                "z" -> rotateAroundZ(vector, reader)
                "yaw-pitch" -> rotateAroundYawAndPitch(vector, reader)
                "axis" -> rotateAroundAxis(vector, reader)
                "non-unit-axis" -> rotateAroundNonUnitAxis(vector, reader)
                else -> error("Unknown argument \"$it\" at vector rotate action.")
            }
        }

        fun rotateAroundX(vector: LiveData<Vector>?, reader: QuestReader): Handler {
            val angle = reader.readDouble()
            val reproduced = this.isReproduced(reader)
            return transfer { previous ->
                val vec = previous ?: vector?.get(this, Vector()) ?: error("No vector selected.")
                vec.let {
                    if (reproduced) it.clone() else it
                }.rotateAroundX(angle.get(this, 0.0))
            }
        }

        fun rotateAroundY(vector: LiveData<Vector>?, reader: QuestReader): Handler {
            val angle = reader.readDouble()
            val reproduced = this.isReproduced(reader)
            return transfer { previous ->
                val vec = previous ?: vector?.get(this, Vector()) ?: error("No vector selected.")
                vec.let {
                    if (reproduced) it.clone() else it
                }.rotateAroundY(angle.get(this, 0.0))
            }
        }

        fun rotateAroundZ(vector: LiveData<Vector>?, reader: QuestReader): Handler {
            val angle = reader.readDouble()
            val reproduced = this.isReproduced(reader)
            return transfer { previous ->
                val vec = previous ?: vector?.get(this, Vector()) ?: error("No vector selected.")
                vec.let {
                    if (reproduced) it.clone() else it
                }.rotateAroundZ(angle.get(this, 0.0))
            }
        }

        fun rotateAroundYawAndPitch(vector: LiveData<Vector>?, reader: QuestReader): Handler {
            reader.expect("by")
            val yaw = reader.readDouble()
            val pitch = reader.readDouble()
            val reproduced = this.isReproduced(reader)
            return transfer { previous ->
                val vec = previous ?: vector?.get(this, Vector()) ?: error("No vector selected.")
                val newVec = VectorUtils.rotateVector(vec, yaw.get(this, 0.0).toFloat(), pitch.get(this, 0.0).toFloat())
                if (reproduced) {
                    newVec
                } else {
                    vec.copy(newVec)
                }
            }
        }

        fun rotateAroundAxis(vector: LiveData<Vector>?, reader: QuestReader): Handler {
            val axis = this.expectVector(reader, "by", "using")
            val angle = reader.readDouble()
            val reproduced = this.isReproduced(reader)
            return transfer { previous ->
                val vec = previous ?: vector?.get(this, Vector()) ?: error("No vector selected.")
                vec.let {
                    if (reproduced) it.clone() else it
                }.rotateAroundAxis(axis.get(this, Vector()), angle.get(this, 0.0))
            }
        }

        fun rotateAroundNonUnitAxis(vector: LiveData<Vector>?, reader: QuestReader): Handler {
            val axis = this.expectVector(reader, "by", "using")
            val angle = reader.readDouble()
            val reproduced = this.isReproduced(reader)
            return transfer { previous ->
                val vec = previous ?: vector?.get(this, Vector()) ?: error("No vector selected.")
                vec.let {
                    if (reproduced) it.clone() else it
                }.rotateAroundNonUnitAxis(axis.get(this, Vector()), angle.get(this, 0.0))
            }
        }

        private fun handle(func: ScriptFrame.(previous: Vector?) -> Any): Handler {
            return object : Handler {
                override fun handle(frame: ScriptFrame, previous: Vector?): Any {
                    return func(frame, previous)
                }
            }
        }

        private fun transfer(func: ScriptFrame.(previous: Vector?) -> Vector): Handler {
            return object : TransferHandler {
                override fun handle(frame: ScriptFrame, previous: Vector?): Vector {
                    return func(frame, previous)
                }
            }
        }

        /**
         * 读取 Vector 数据
         * 主要用于修改操作
         * */
        private fun readVector(reader: QuestReader, expect: String): LiveData<Vector> {
            return if (reader.hasNextToken("to")) {
                val amount = reader.readDouble()
                VectorLiveData(Triple(amount, amount, amount))
            } else if (reader.hasNextToken(expect)) {
                VectorLiveData(reader.nextBlock())
            } else {
                val x = reader.readDouble()
                val y = reader.readDouble()
                val z = reader.readDouble()
                VectorLiveData(Triple(x, y, z))
            }
        }

        /**
         * 读取期望 Vector 数据
         * 主要用于运算
         *
         * @param expect 期望前缀，若找不到则使用 other 来构建 Vector
         * @param other 用来标识构建 Vector 的前缀
         * */
        private fun expectVector(reader: QuestReader, expect: String, other: String): LiveData<Vector> {
            return if (reader.hasNextToken(expect)) {
                VectorLiveData(reader.nextBlock())
            } else {
                reader.expect(other)
                val x = reader.readDouble()
                val y = reader.readDouble()
                val z = reader.readDouble()
                VectorLiveData(Triple(x, y, z))
            }
        }

        private fun isReproduced(reader: QuestReader): Boolean {
            return !reader.hasNextToken("not-reproduced", "not-rep", "not-clone", "-n")
        }
    }
}
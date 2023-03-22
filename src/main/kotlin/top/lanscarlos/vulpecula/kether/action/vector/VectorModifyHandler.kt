package top.lanscarlos.vulpecula.kether.action.vector

import taboolib.common.util.Vector
import taboolib.library.kether.QuestReader
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.kether.live.VectorLiveData
import top.lanscarlos.vulpecula.utils.hasNextToken
import top.lanscarlos.vulpecula.utils.nextBlock
import top.lanscarlos.vulpecula.kether.live.readDouble
import top.lanscarlos.vulpecula.kether.live.readVector

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.vector
 *
 * @author Lanscarlos
 * @since 2022-11-14 17:46
 */
@Deprecated("")
object VectorModifyHandler : ActionVector.Reader {

    override val name: Array<String> = arrayOf(
        "modify", "set",
        "add", "subtract", "sub", "multiply", "mul", "divide", "div"
    )

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionVector.Handler {
        val source = if (isRoot) reader.readVector(false) else null
        when (input) {
            "modify", "set" -> {
                val other = reader.expectVectorBy("with", "copy", "by")
                val reproduced = reader.isReproduced()

                return acceptTransferFuture(source, reproduced) { vector ->
                    other.get(this, vector).thenApply {
                        vector.copy(it)
                    }
                }
            }
            "add", "multiply", "mul" -> {
                val other = reader.expectVectorBy("with", "and")
                val reproduced = reader.isReproduced()

                return acceptTransferFuture(source, reproduced) { vector ->
                    other.get(this, Vector()).thenApply {
                        when (input) {
                            "add" -> vector.add(it)
                            "multiply", "mul" -> vector.multiply(it)
                            else -> vector
                        }
                    }
                }
            }
            "subtract", "sub", "divide", "div" -> {
                val other = reader.expectVectorBy("by")
                val reproduced = reader.isReproduced()

                return acceptTransferFuture(source, reproduced) { vector ->
                    other.get(this, Vector()).thenApply {
                        when (input) {
                            "subtract", "sub" -> vector.subtract(it)
                            "divide", "div" -> vector.divide(it)
                            else -> vector
                        }
                    }
                }
            }
            else -> {
                return acceptTransferNow(source, false) { vector -> vector }
            }
        }
    }

    /**
     * 读取期望 Vector 数据
     * 主要用于运算
     * */
    private fun QuestReader.expectVectorBy(vararg expect: String): LiveData<Vector> {
        return if (this.hasNextToken("to")) {
            val amount = this.readDouble()
            VectorLiveData(Triple(amount, amount, amount))
        } else if (this.hasNextToken(*expect)) {
            VectorLiveData(this.nextBlock())
        } else {
            val x = this.readDouble()
            val y = this.readDouble()
            val z = this.readDouble()
            VectorLiveData(Triple(x, y, z))
        }
    }
}
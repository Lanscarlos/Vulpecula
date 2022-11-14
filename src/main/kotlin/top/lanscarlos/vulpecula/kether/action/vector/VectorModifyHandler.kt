package top.lanscarlos.vulpecula.kether.action.vector

import taboolib.common.util.Vector
import taboolib.library.kether.QuestReader
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.kether.live.VectorLiveData
import top.lanscarlos.vulpecula.utils.hasNextToken
import top.lanscarlos.vulpecula.utils.nextBlock
import top.lanscarlos.vulpecula.utils.readDouble
import top.lanscarlos.vulpecula.utils.readVector

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.vector
 *
 * @author Lanscarlos
 * @since 2022-11-14 17:46
 */
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

                return acceptTransfer(source, reproduced) { vector ->
                    vector.copy(other.get(this, vector))
                }
            }
            "add", "multiply", "mul" -> {
                val other = reader.expectVectorBy("with", "and")
                val reproduced = reader.isReproduced()

                return acceptTransfer(source, reproduced) { vector ->
                    when (input) {
                        "add" -> vector.add(other.get(this, Vector()))
                        "multiply", "mul" -> vector.multiply(other.get(this, Vector()))
                        else -> vector
                    }
                }
            }
            "subtract", "sub", "divide", "div" -> {
                val other = reader.expectVectorBy("by")
                val reproduced = reader.isReproduced()

                return acceptTransfer(source, reproduced) { vector ->
                    when (input) {
                        "subtract", "sub" -> vector.subtract(other.get(this, Vector()))
                        "divide", "div" -> vector.divide(other.get(this, Vector()))
                        else -> vector
                    }
                }
            }
            else -> {
                return acceptTransfer(source, false) { vector -> vector }
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
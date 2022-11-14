package top.lanscarlos.vulpecula.kether.action.vector

import taboolib.common.util.Vector
import taboolib.library.kether.QuestReader
import taboolib.module.effect.VectorUtils
import top.lanscarlos.vulpecula.kether.live.VectorLiveData
import top.lanscarlos.vulpecula.utils.hasNextToken
import top.lanscarlos.vulpecula.utils.nextBlock
import top.lanscarlos.vulpecula.utils.readDouble

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.vector
 *
 * @author Lanscarlos
 * @since 2022-11-14 19:07
 */
object VectorRotateHandler : ActionVector.Reader {

    override val name: Array<String> = arrayOf(
        "rotate"
    )

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionVector.Handler {
        val source = if (isRoot) VectorLiveData(reader.nextBlock()) else null
        when (val it = reader.nextToken()) {
            "x", "y", "z" -> {
                val angle = reader.readDouble()
                val reproduced = reader.isReproduced()

                return acceptTransfer(source, reproduced) { vector ->
                    when (it) {
                        "x" -> vector.rotateAroundX(angle.get(this, 0.0))
                        "y" -> vector.rotateAroundY(angle.get(this, 0.0))
                        "z" -> vector.rotateAroundZ(angle.get(this, 0.0))
                        else -> vector
                    }
                }
            }
            "yaw-pitch", "y-p" -> {
                reader.hasNextToken("by")
                val yaw = reader.readDouble()
                val pitch = reader.readDouble()
                val reproduced = reader.isReproduced()

                return acceptTransfer(source, false) { vector ->
                    val newVector = VectorUtils.rotateVector(
                        vector,
                        yaw.get(this, 0.0).toFloat(),
                        pitch.get(this, 0.0).toFloat()
                    )
                    if (reproduced) newVector else vector.copy(newVector)
                }
            }
            "axis", "non-unit-axis", "n-axis" -> {
                val axis = reader.expectVector("by", "using")
                val angle = reader.readDouble()
                val reproduced = reader.isReproduced()

                return acceptTransfer(source, reproduced) { vector ->
                    when (it) {
                        "axis" -> vector.rotateAroundAxis(axis.get(this, Vector()), angle.get(this, 0.0))
                        "non-unit-axis", "n-axis" -> vector.rotateAroundNonUnitAxis(axis.get(this, Vector()), angle.get(this, 0.0))
                        else -> vector
                    }
                }
            }
            else -> error("Unknown argument \"$it\" at vector rotate action.")
        }
    }
}
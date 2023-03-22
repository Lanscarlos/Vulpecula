package top.lanscarlos.vulpecula.kether.action.vector

import taboolib.common.util.Vector
import taboolib.library.kether.QuestReader
import taboolib.module.effect.utils.VectorUtils
import top.lanscarlos.vulpecula.kether.live.VectorLiveData
import top.lanscarlos.vulpecula.kether.live.readDouble
import top.lanscarlos.vulpecula.utils.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.vector
 *
 * @author Lanscarlos
 * @since 2022-11-14 19:07
 */
@Deprecated("")
object VectorRotateHandler : ActionVector.Reader {

    override val name: Array<String> = arrayOf(
        "rotate"
    )

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionVector.Handler {
        val source = if (isRoot) VectorLiveData(reader.nextBlock()) else null
        when (val rotate = reader.nextToken()) {
            "x", "y", "z" -> {
                val angle = reader.readDouble()
                val reproduced = reader.isReproduced()

                return acceptTransferFuture(source, reproduced) { vector ->
                    angle.get(this, 0.0).thenApply {
                        when (rotate) {
                            "x" -> vector.rotateAroundX(it)
                            "y" -> vector.rotateAroundY(it)
                            "z" -> vector.rotateAroundZ(it)
                            else -> vector
                        }
                    }
                }
            }
            "yaw-pitch", "y-p" -> {
                reader.hasNextToken("by")
                val yaw = reader.readDouble()
                val pitch = reader.readDouble()
                val reproduced = reader.isReproduced()

                return acceptTransferFuture(source, false) { vector ->
                    listOf(
                        yaw.getOrNull(this),
                        pitch.getOrNull(this)
                    ).thenTake().thenApply {
                        val newVector = VectorUtils.rotateVector(
                            vector,
                            it[0].coerceFloat(0f),
                            it[1].coerceFloat(0f)
                        )
                        if (reproduced) newVector else vector.copy(newVector)
                    }
                }
            }
            "axis", "non-unit-axis", "n-axis" -> {
                val axis = reader.expectVector("by", "using")
                val angle = reader.readDouble()
                val reproduced = reader.isReproduced()

                return acceptTransferFuture(source, reproduced) { vector ->
                    listOf(
                        axis.get(this, Vector()),
                        angle.getOrNull(this,)
                    ).thenTake().thenApply {
                        when (rotate) {
                            "axis" -> vector.rotateAroundAxis(it[0] as Vector, it[1].coerceDouble(0.0))
                            "non-unit-axis", "n-axis" -> vector.rotateAroundNonUnitAxis(it[0] as Vector, it[1].coerceDouble(0.0))
                            else -> vector
                        }
                    }
                }
            }
            else -> error("Unknown argument \"$rotate\" at vector rotate action.")
        }
    }
}
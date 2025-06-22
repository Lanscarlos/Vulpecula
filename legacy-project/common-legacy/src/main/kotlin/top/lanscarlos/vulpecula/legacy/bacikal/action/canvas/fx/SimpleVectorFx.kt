package top.lanscarlos.vulpecula.legacy.bacikal.action.canvas.fx

import taboolib.common.util.Vector

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.canvas.fx
 *
 * @author Lanscarlos
 * @since 2023-06-30 11:28
 */
class SimpleVectorFx(
    val x: Number?,
    val y: Number?,
    val z: Number?
) : VectorFx<Unit>() {

    constructor(vector: Vector): this(vector.x, vector.y, vector.z)

    override fun calculate(): Vector {
        return Vector(x?.toDouble() ?: 0.0, y?.toDouble() ?: 0.0, z?.toDouble() ?: 0.0)
    }

    override fun calculate(input: Unit): Vector {
        return calculate()
    }

    override fun copy(): SimpleVectorFx {
        return SimpleVectorFx(x, y, z)
    }
}
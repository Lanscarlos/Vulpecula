package top.lanscarlos.vulpecula.legacy.bacikal.action.canvas.fx

import top.lanscarlos.vulpecula.legacy.bacikal.Bacikal
import top.lanscarlos.vulpecula.legacy.bacikal.BacikalReader

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.canvas.fx
 *
 * @author Lanscarlos
 * @since 2023-06-29 11:41
 */
class LinearFx(
    val m: Double, // 斜率
    val b: Double // 截距
) : DecimalFx<Double>() {

    var x = 0.0

    override fun calculate(): Double {
        return calculate(x++)
    }

    override fun calculate(input: Double): Double {
        return m * input + b
    }

    override fun copy(): LinearFx {
        return LinearFx(m, b)
    }

    companion object : ActionFx.Resolver {

        override val name = arrayOf("linear", "l")

        override fun resolve(reader: BacikalReader): Bacikal.Parser<Fx<*, *>> {
            return reader.run {
                combine(
                    argument("m", then = double(display = "fx linear m"), def = 1.0),
                    argument("b", then = double(display = "fx linear b"), def = 0.0)
                ) { m, b ->
                    LinearFx(m, b)
                }
            }
        }
    }
}
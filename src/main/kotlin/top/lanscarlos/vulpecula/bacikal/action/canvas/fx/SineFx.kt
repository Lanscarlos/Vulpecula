package top.lanscarlos.vulpecula.bacikal.action.canvas.fx

import top.lanscarlos.vulpecula.bacikal.Bacikal
import top.lanscarlos.vulpecula.bacikal.BacikalReader
import kotlin.math.sin

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.canvas.fx
 *
 * @author Lanscarlos
 * @since 2023-06-29 11:43
 */
class SineFx(
    val a: Double, // 振幅
    val w: Double, // 周期
    val init: Double, // 初相位
    val b: Double // 偏移
) : DecimalFx<Double>() {

    var x = 0.0

    override fun calculate(): Double {
        return calculate(x++)
    }

    override fun calculate(input: Double): Double {
        return a * sin(Math.toRadians(w * input + init)) + b
    }

    override fun copy(): SineFx {
        return SineFx(a, w, init, b)
    }

    companion object : ActionFx.Resolver {

        override val name = arrayOf("sine", "sin", "s")

        override fun resolve(reader: BacikalReader): Bacikal.Parser<Fx<*, *>> {
            return reader.run {
                combine(
                    argument("a", then = double(display = "fx sine A"), def = 1.0),
                    argument("w", then = double(display = "fx sine w"), def = 1.0),
                    argument("init", "i", then = double(display = "fx sine init"), def = 0.0),
                    argument("b", then = double(display = "fx sine b"), def = 0.0)
                ) { a, w, init, b ->
                    SineFx(a, w, init, b)
                }
            }
        }
    }
}
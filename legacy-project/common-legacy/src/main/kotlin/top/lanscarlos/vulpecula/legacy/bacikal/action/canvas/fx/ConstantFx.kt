package top.lanscarlos.vulpecula.legacy.bacikal.action.canvas.fx

import top.lanscarlos.vulpecula.legacy.bacikal.Bacikal
import top.lanscarlos.vulpecula.legacy.bacikal.BacikalReader

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.canvas.fx
 *
 * @author Lanscarlos
 * @since 2023-06-29 11:45
 */
class ConstantFx(val value: Double) : DecimalFx<Double>() {

    override fun calculate(): Double {
        return value
    }

    override fun calculate(input: Double): Double {
        return value
    }

    override fun copy(): ConstantFx {
        return ConstantFx(value)
    }

    companion object : ActionFx.Resolver {

        override val name = arrayOf("constant", "c")

        override fun resolve(reader: BacikalReader): Bacikal.Parser<Fx<*, *>> {
            return reader.run {
                combine(
                    double(display = "fx constant")
                ) { value ->
                    ConstantFx(value)
                }
            }
        }
    }
}
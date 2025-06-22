package top.lanscarlos.vulpecula.legacy.bacikal.action.canvas.fx

import top.lanscarlos.vulpecula.legacy.bacikal.Bacikal
import top.lanscarlos.vulpecula.legacy.bacikal.BacikalReader
import kotlin.random.Random

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.canvas.fx
 *
 * @author Lanscarlos
 * @since 2023-07-08 13:40
 */
class RandomFx(val seed: Long, val min: Double, val max: Double) : DecimalFx<Random>() {

    val random = Random(seed)

    override fun calculate(): Double {
        return calculate(random)
    }

    override fun calculate(input: Random): Double {
        return input.nextDouble(min, max)
    }

    override fun copy(): RandomFx {
        return RandomFx(seed, min, max)
    }

    companion object : ActionFx.Resolver {

        override val name = arrayOf("random", "r")

        override fun resolve(reader: BacikalReader): Bacikal.Parser<Fx<*, *>> {
            return reader.run {
                combine(
                    argument("seed", then = long(display = "fx random seed")),
                    argument("min", then = double(display = "fx random min"), def = 0.0),
                    argument("max", then = double(display = "fx random max"), def = 1.0)
                ) { seed, min, max ->
                    RandomFx(seed ?: System.currentTimeMillis(), min, max)
                }
            }
        }
    }
}
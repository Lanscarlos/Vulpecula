package top.lanscarlos.vulpecula.bacikal.action.math

import top.lanscarlos.vulpecula.bacikal.BacikalParser
import top.lanscarlos.vulpecula.bacikal.bacikalSwitch

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.math
 *
 * @author Lanscarlos
 * @since 2023-03-19 23:52
 */
object ActionCoerce {

    /**
     *
     * coerce &n in &min &max
     *
     * coerce &n min &min
     * coerce &n least &min
     *
     * coerce &n max &max
     * coerce &n most &max
     *
     * */
    @BacikalParser("coerce")
    fun parser() = bacikalSwitch {
        val any = any().accept(this)

        /*
        * coerce &source in &min between &max
        * coerce &source in &min to &max
        * coerce &source in &min &max
        * */
        case("in") {
            combine(
                any,
                double(),
                trim("between", "to", then = double())
            ) { source, min, max ->
                when(source) {
                    is Short -> source.coerceIn(min.toInt().toShort(), max.toInt().toShort())
                    is Int -> source.coerceIn(min.toInt(), max.toInt())
                    is Long -> source.coerceIn(min.toLong(), max.toLong())
                    is Float -> source.coerceIn(min.toFloat(), max.toFloat())
                    is Double -> source.coerceIn(min, max)
                    is String -> source.toDoubleOrNull()?.coerceIn(min, max)
                    else -> source
                }
            }
        }

        /*
        * coerce &source least &limit
        * coerce &source min &limit
        * */
        case("least", "min") {
            combine(
                any,
                double()
            ) { source, limit ->
                when(source) {
                    is Short -> source.coerceAtLeast(limit.toInt().toShort())
                    is Int -> source.coerceAtLeast(limit.toInt())
                    is Long -> source.coerceAtLeast(limit.toLong())
                    is Float -> source.coerceAtLeast(limit.toFloat())
                    is Double -> source.coerceAtLeast(limit)
                    is String -> source.toDoubleOrNull()?.coerceAtLeast(limit)
                    else -> source
                }
            }
        }

        /*
        * coerce &source most &limit
        * coerce &source max &limit
        * */
        case("most", "max") {
            combine(
                any,
                double()
            ) { source, limit ->
                when(source) {
                    is Short -> source.coerceAtMost(limit.toInt().toShort())
                    is Int -> source.coerceAtMost(limit.toInt())
                    is Long -> source.coerceAtMost(limit.toLong())
                    is Float -> source.coerceAtMost(limit.toFloat())
                    is Double -> source.coerceAtMost(limit)
                    is String -> source.toDoubleOrNull()?.coerceAtMost(limit)
                    else -> source
                }
            }
        }
    }
}
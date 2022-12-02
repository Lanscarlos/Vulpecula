package top.lanscarlos.vulpecula.kether.action

import taboolib.module.kether.actionTake
import taboolib.module.kether.scriptParser
import taboolib.module.kether.switch
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.kether.live.readDouble
import top.lanscarlos.vulpecula.utils.coerceDouble

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action
 *
 * @author Lanscarlos
 * @since 2022-11-10 21:32
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
    @VulKetherParser(id = "coerce", name = ["coerce"])
    fun parser() = scriptParser { reader ->
        val next = reader.readDouble()
        reader.switch {
            case("in") {
                val min = reader.readDouble()
                val max = reader.readDouble()
                actionTake {
                    next.thenApplyOrNull(this, min.getOrNull(this), max.getOrNull(this)) {
                        this?.coerceIn(it.first().coerceDouble(0.0), it.last().coerceDouble(0.0)) ?: 0.0
                    }
                }
            }
            case("min", "least") {
                val min = reader.readDouble()
                actionTake {
                    next.thenApplyOrNull(this, min.getOrNull(this)) {
                        this?.coerceAtLeast(it.first().coerceDouble(0.0)) ?: 0.0
                    }
                }
            }
            case("max", "most") {
                val max = reader.readDouble()
                actionTake {
                    next.thenApplyOrNull(this, max.getOrNull(this)) {
                        this?.coerceAtMost(it.first().coerceDouble(0.0)) ?: 0.0
                    }
                }
            }
        }
    }
}
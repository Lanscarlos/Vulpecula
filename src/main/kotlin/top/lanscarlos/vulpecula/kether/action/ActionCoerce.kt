package top.lanscarlos.vulpecula.kether.action

import taboolib.module.kether.actionNow
import taboolib.module.kether.scriptParser
import taboolib.module.kether.switch
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.utils.readDouble

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
    @VulKetherParser(id = "coerce", name = ["pow"])
    fun parser() = scriptParser { reader ->
        val next = reader.readDouble()
        reader.switch {
            case("in") {
                val min = reader.readDouble()
                val max = reader.readDouble()
                actionNow {
                    next.get(this, 0.0).coerceIn(
                        min.get(this, 0.0),
                        max.get(this, 0.0)
                    )
                }
            }
            case("min", "least") {
                val min = reader.readDouble()
                actionNow {
                    next.get(this, 0.0).coerceAtLeast(
                        min.get(this, 0.0)
                    )
                }
            }
            case("max", "most") {
                val max = reader.readDouble()
                actionNow {
                    next.get(this, 0.0).coerceAtMost(
                        max.get(this, 0.0)
                    )
                }
            }
        }
    }
}
package top.lanscarlos.vulpecula.kether.action.math

import taboolib.module.kether.actionTake
import taboolib.module.kether.scriptParser
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.kether.live.readDouble
import top.lanscarlos.vulpecula.utils.coerceDouble
import kotlin.math.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action
 *
 * @author Lanscarlos
 * @since 2022-11-10 21:04
 */
object ActionMaths {

    @VulKetherParser(id = "pow", name = ["pow"])
    fun powParser() = scriptParser { reader ->
        val base = reader.readDouble()
        val power = reader.readDouble()
        actionTake {
            base.thenApplyOrNull(this, power.getOrNull(this)) {
                this?.pow(it.first().coerceDouble(0.0)) ?: 0.0
            }
        }
    }

    @VulKetherParser(id = "sqrt", name = ["sqrt"])
    fun sqrtParser() = scriptParser { reader ->
        val next = reader.readDouble()
        actionTake { next.get(this, 0.0).thenApply { sqrt(it) } }
    }

    @VulKetherParser(id = "ceil", name = ["ceil"])
    fun ceilParser() = scriptParser { reader ->
        val next = reader.readDouble()
        actionTake { next.get(this, 0.0).thenApply { ceil(it) } }
    }

    @VulKetherParser(id = "floor", name = ["floor"])
    fun floorParser() = scriptParser { reader ->
        val next = reader.readDouble()
        actionTake { next.get(this, 0.0).thenApply { floor(it) } }
    }

    @VulKetherParser(id = "log", name = ["log"])
    fun logParser() = scriptParser { reader ->
        val base = reader.readDouble() // 底数
        val natural = reader.readDouble() // 真数
        actionTake {
            base.thenApplyOrNull(this, natural.getOrNull(this)) {
                log(it.first().coerceDouble(0.0), this ?: 0.0)
            }
        }
    }

    @VulKetherParser(id = "ln", name = ["ln"])
    fun lnParser() = scriptParser { reader ->
        val next = reader.readDouble()
        actionTake { next.get(this, 0.0).thenApply { ln(it) } }
    }

    @VulKetherParser(id = "lg", name = ["lg"])
    fun lgParser() = scriptParser { reader ->
        val next = reader.readDouble()
        actionTake { next.get(this, 0.0).thenApply { log10(it) } }
    }

    @VulKetherParser(id = "radian", name = ["radian", "rad"])
    fun radianParser() = scriptParser { reader ->
        val next = reader.readDouble()
        actionTake { next.get(this, 0.0).thenApply { Math.toRadians(it) } }
    }

    @VulKetherParser(id = "sin", name = ["sin"])
    fun sinParser() = scriptParser { reader ->
        val next = reader.readDouble()
        actionTake { next.get(this, 0.0).thenApply { sin(it) } }
    }

    @VulKetherParser(id = "cos", name = ["cos"])
    fun cosParser() = scriptParser { reader ->
        val next = reader.readDouble()
        actionTake { next.get(this, 0.0).thenApply { cos(it) } }
    }

    @VulKetherParser(id = "tan", name = ["tan"])
    fun tanParser() = scriptParser { reader ->
        val next = reader.readDouble()
        actionTake { next.get(this, 0.0).thenApply { tan(it) } }
    }
}
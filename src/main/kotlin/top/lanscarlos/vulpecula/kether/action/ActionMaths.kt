package top.lanscarlos.vulpecula.kether.action

import taboolib.module.kether.actionNow
import taboolib.module.kether.scriptParser
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.utils.readDouble
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
    fun powParser() = scriptParser {
        val base = it.readDouble()
        val power = it.readDouble()
        actionNow {
            base.get(this, 0.0).pow(power.get(this, 0.0))
        }
    }

    @VulKetherParser(id = "sqrt", name = ["sqrt"])
    fun sqrtParser() = scriptParser {
        val next = it.readDouble()
        actionNow { sqrt(next.get(this, 0.0)) }
    }

    @VulKetherParser(id = "ceil", name = ["ceil"])
    fun ceilParser() = scriptParser {
        val next = it.readDouble()
        actionNow { ceil(next.get(this, 0.0)) }
    }

    @VulKetherParser(id = "floor", name = ["floor"])
    fun floorParser() = scriptParser {
        val next = it.readDouble()
        actionNow { floor(next.get(this, 0.0)) }
    }

    @VulKetherParser(id = "log", name = ["log"])
    fun logParser() = scriptParser {
        val base = it.readDouble() // 底数
        val natural = it.readDouble() // 真数
        actionNow { log(natural.get(this, 0.0), base.get(this, 0.0)) }
    }

    @VulKetherParser(id = "ln", name = ["ln"])
    fun lnParser() = scriptParser {
        val next = it.readDouble()
        actionNow { ln(next.get(this, 0.0)) }
    }

    @VulKetherParser(id = "lg", name = ["lg"])
    fun lgParser() = scriptParser {
        val next = it.readDouble()
        actionNow { log10(next.get(this, 0.0)) }
    }

    @VulKetherParser(id = "radian", name = ["radian", "rad"])
    fun radianParser() = scriptParser {
        val next = it.readDouble()
        actionNow { Math.toRadians(next.get(this, 0.0)) }
    }

    @VulKetherParser(id = "sin", name = ["sin"])
    fun sinParser() = scriptParser {
        val next = it.readDouble()
        actionNow { sin(next.get(this, 0.0)) }
    }

    @VulKetherParser(id = "cos", name = ["cos"])
    fun cosParser() = scriptParser {
        val next = it.readDouble()
        actionNow { cos(next.get(this, 0.0)) }
    }

    @VulKetherParser(id = "tan", name = ["tan"])
    fun tanParser() = scriptParser {
        val next = it.readDouble()
        actionNow { tan(next.get(this, 0.0)) }
    }
}
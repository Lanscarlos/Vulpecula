package top.lanscarlos.vulpecula.legacy.bacikal.action.math

import top.lanscarlos.vulpecula.legacy.bacikal.BacikalParser
import top.lanscarlos.vulpecula.legacy.bacikal.bacikal
import kotlin.math.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.math
 *
 * @author Lanscarlos
 * @since 2023-03-26 10:25
 */
object ActionMaths {

    @BacikalParser(id = "pow", aliases = ["pow"])
    fun powParser() = bacikal {
        combine(
            double(),
            double()
        ) { base, power -> base.pow(power) }
    }

    @BacikalParser(id = "sqrt", aliases = ["sqrt"])
    fun sqrtParser() = bacikal {
        combine(
            double()
        ) { base -> sqrt(base) }
    }

    @BacikalParser(id = "ceil", aliases = ["ceil"])
    fun ceilParser() = bacikal {
        combine(
            double()
        ) { base -> ceil(base) }
    }

    @BacikalParser(id = "ln", aliases = ["ln"])
    fun lnParser() = bacikal {
        combine(
            double()
        ) { base -> ln(base) }
    }

    @BacikalParser(id = "lg", aliases = ["lg"])
    fun lgParser() = bacikal {
        combine(
            double()
        ) { base -> log10(base) }
    }

    @BacikalParser(id = "radian", aliases = ["radian", "rad"])
    fun radianParser() = bacikal {
        combine(
            double()
        ) { base -> Math.toRadians(base) }
    }

    @BacikalParser(id = "sin", aliases = ["sin"])
    fun sinParser() = bacikal {
        combine(
            double()
        ) { base -> sin(base) }
    }

    @BacikalParser(id = "cos", aliases = ["cos"])
    fun cosParser() = bacikal {
        combine(
            double()
        ) { base -> cos(base) }
    }

    @BacikalParser(id = "tan", aliases = ["tan"])
    fun tanParser() = bacikal {
        combine(
            double()
        ) { base -> tan(base) }
    }

}
package top.lanscarlos.vulpecula.bacikal.action.math

import top.lanscarlos.vulpecula.bacikal.BacikalParser
import top.lanscarlos.vulpecula.bacikal.bacikal
import kotlin.math.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.math
 *
 * @author Lanscarlos
 * @since 2023-03-26 10:25
 */
object ActionMaths {

    @BacikalParser(id = "pow", name = ["pow"])
    fun powParser() = bacikal {
        combine(
            double(),
            double()
        ) { base, power -> base.pow(power) }
    }

    @BacikalParser(id = "sqrt", name = ["sqrt"])
    fun sqrtParser() = bacikal {
        combine(
            double()
        ) { base -> sqrt(base) }
    }

    @BacikalParser(id = "ceil", name = ["ceil"])
    fun ceilParser() = bacikal {
        combine(
            double()
        ) { base -> ceil(base) }
    }

    @BacikalParser(id = "ln", name = ["ln"])
    fun lnParser() = bacikal {
        combine(
            double()
        ) { base -> ln(base) }
    }

    @BacikalParser(id = "lg", name = ["lg"])
    fun lgParser() = bacikal {
        combine(
            double()
        ) { base -> log10(base) }
    }

    @BacikalParser(id = "radian", name = ["radian", "rad"])
    fun radianParser() = bacikal {
        combine(
            double()
        ) { base -> Math.toRadians(base) }
    }

    @BacikalParser(id = "sin", name = ["sin"])
    fun sinParser() = bacikal {
        combine(
            double()
        ) { base -> sin(base) }
    }

    @BacikalParser(id = "cos", name = ["cos"])
    fun cosParser() = bacikal {
        combine(
            double()
        ) { base -> cos(base) }
    }

    @BacikalParser(id = "tan", name = ["tan"])
    fun tanParser() = bacikal {
        combine(
            double()
        ) { base -> tan(base) }
    }

}
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

    @BacikalParser("pow")
    fun powParser() = bacikal {
        combine(
            double(),
            double()
        ) { base, power -> base.pow(power) }
    }

    @BacikalParser("sqrt")
    fun sqrtParser() = bacikal {
        combine(
            double()
        ) { base -> sqrt(base) }
    }

    @BacikalParser("ceil")
    fun ceilParser() = bacikal {
        combine(
            double()
        ) { base -> ceil(base) }
    }

    @BacikalParser("ln")
    fun lnParser() = bacikal {
        combine(
            double()
        ) { base -> ln(base) }
    }

    @BacikalParser("lg")
    fun lgParser() = bacikal {
        combine(
            double()
        ) { base -> log10(base) }
    }

    @BacikalParser("radian")
    fun radianParser() = bacikal {
        combine(
            double()
        ) { base -> Math.toRadians(base) }
    }

    @BacikalParser("sin")
    fun sinParser() = bacikal {
        combine(
            double()
        ) { base -> sin(base) }
    }

    @BacikalParser("cos")
    fun cosParser() = bacikal {
        combine(
            double()
        ) { base -> cos(base) }
    }

    @BacikalParser("tan")
    fun tanParser() = bacikal {
        combine(
            double()
        ) { base -> tan(base) }
    }

}
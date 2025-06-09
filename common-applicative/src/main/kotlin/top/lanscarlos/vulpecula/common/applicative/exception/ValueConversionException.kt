package top.lanscarlos.vulpecula.common.applicative.exception

import top.lanscarlos.vulpecula.common.core.utils.asLang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.applicative.exception
 *
 * @author Lanscarlos
 * @since 2025/5/29 11:54
 */
class ValueConversionException(val value: String, val target: Class<*>) : RuntimeException() {

    override val message: String = asLang("common-applicative-exception-invalid-value", value, target.name)

}
package top.lanscarlos.vulpecula.common.exception

import top.lanscarlos.vulpecula.common.utils.asLang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.core.exception
 *
 * @author Lanscarlos
 * @since 2025/6/4 11:08
 */
class InvalidTimeUnitException(value: String) : RuntimeException() {

    override val message: String = asLang("common-core-exception-invalid-time-unit", value)

}
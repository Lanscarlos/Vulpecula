package top.lanscarlos.vulpecula.common.core.exception

import top.lanscarlos.vulpecula.common.core.utils.asLang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.core.exception
 *
 * @author Lanscarlos
 * @since 2025/6/9 11:07
 */
class InvalidArgumentFormatException(val value: String) : RuntimeException() {

    override val message: String = asLang("common-core-exception-invalid-argument-format", value)

}
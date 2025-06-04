package top.lanscarlos.vulpecula.common.core.exception

import top.lanscarlos.vulpecula.common.lang.MessageService

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.core.exception
 *
 * @author Lanscarlos
 * @since 2025/6/4 11:07
 */
class InvalidTimeFormatException(value: String) : RuntimeException() {

    override val message: String = MessageService.asLang("common-core-exception-invalid-time-format", value)

}
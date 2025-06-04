package top.lanscarlos.vulpecula.common.core.exception

import top.lanscarlos.vulpecula.common.lang.MessageService

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.core.exception
 *
 * @author Lanscarlos
 * @since 2025/6/4 10:52
 */
class InvalidTypeException(value: Any) : RuntimeException() {

    override val message: String = MessageService.asLang("common-core-exception-invalid-type", value::class.java.name)

}
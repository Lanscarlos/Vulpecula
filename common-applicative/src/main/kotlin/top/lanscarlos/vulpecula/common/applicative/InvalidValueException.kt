package top.lanscarlos.vulpecula.common.applicative

import top.lanscarlos.vulpecula.common.lang.MessageService

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.applicative
 *
 * @author Lanscarlos
 * @since 2025/5/29 11:54
 */
class InvalidValueException(val value: String, val target: Class<*>) : RuntimeException() {

    override val message: String = MessageService.asLang("common-applicative-exception-invalid-value", value, target.name)

}
package top.lanscarlos.vulpecula.module.schedule.exception

import top.lanscarlos.vulpecula.common.message.MessageService

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.schedule.exception
 *
 * @author Lanscarlos
 * @since 2025/5/28 11:25
 */
class FieldNotFoundException(id: String, field: String) : RuntimeException() {

    override val message: String = MessageService.asLang("module-schedule-exception-field-not-found", id, field)

}
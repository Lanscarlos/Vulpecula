package top.lanscarlos.vulpecula.module.schedule.exception

import top.lanscarlos.vulpecula.common.core.utils.asLang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.schedule.exception
 *
 * @author Lanscarlos
 * @since 2025/6/7
 */
class InvalidCronException(val value: Any) : RuntimeException() {

    override val message: String = asLang("module-schedule-exception-invalid-cron", value)

}
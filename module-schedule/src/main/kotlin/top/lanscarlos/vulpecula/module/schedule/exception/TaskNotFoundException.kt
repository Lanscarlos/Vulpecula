package top.lanscarlos.vulpecula.module.schedule.exception

import top.lanscarlos.vulpecula.common.utils.asLang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.schedule.exception
 *
 * @author Lanscarlos
 * @since 2025/6/7
 */
class TaskNotFoundException(val pid: String) : RuntimeException() {

    override val message: String = asLang("module-schedule-exception-task-not-found", pid)

}
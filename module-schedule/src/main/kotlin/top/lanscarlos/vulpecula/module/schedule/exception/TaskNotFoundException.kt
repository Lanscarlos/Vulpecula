package top.lanscarlos.vulpecula.module.schedule.exception

import top.lanscarlos.vulpecula.common.exception.DefaultLocalizedException
import top.lanscarlos.vulpecula.common.lang.Lang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.schedule.exception
 *
 * @author Lanscarlos
 * @since 2025/6/7
 */
class TaskNotFoundException(val pid: String) : DefaultLocalizedException(Lang.MODULE_SCHEDULE_EXCEPTION_TASK_NOT_FOUND, arrayOf(pid))

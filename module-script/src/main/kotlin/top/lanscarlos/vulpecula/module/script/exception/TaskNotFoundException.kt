package top.lanscarlos.vulpecula.module.script.exception

import top.lanscarlos.vulpecula.common.exception.DefaultLocalizedException
import top.lanscarlos.vulpecula.common.lang.Lang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.script.exception
 *
 * @author Lanscarlos
 * @since 2025/6/7
 */
class TaskNotFoundException(pid: Long) : DefaultLocalizedException(Lang.MODULE_SCRIPT_TASK_NOT_FOUND, arrayOf(pid))
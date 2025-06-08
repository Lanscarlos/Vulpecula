package top.lanscarlos.vulpecula.module.script.exception

import top.lanscarlos.vulpecula.common.core.utils.asLang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.script.exception
 *
 * @author Lanscarlos
 * @since 2025/6/7
 */
class TaskNotFoundException(val id: Long) : RuntimeException() {

    override val message: String = asLang("module-script-exception-task-not-found", id)

}
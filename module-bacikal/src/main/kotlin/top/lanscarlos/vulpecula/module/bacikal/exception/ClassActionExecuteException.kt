package top.lanscarlos.vulpecula.module.bacikal.exception

import top.lanscarlos.vulpecula.common.core.utils.asLang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.exception
 *
 * @author Lanscarlos
 * @since 2025/6/28
 */
class ClassActionExecuteException(id: String, cause: Throwable) : BacikalException(cause) {

    override val message: String = asLang("module-bacikal-exception-class-action-execute", id, cause.localizedMessage)

}
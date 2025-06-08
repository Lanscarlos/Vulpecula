package top.lanscarlos.vulpecula.module.script.exception

import top.lanscarlos.vulpecula.common.core.utils.asLang
import top.lanscarlos.vulpecula.module.script.Script

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.script.exception
 *
 * @author Lanscarlos
 * @since 2025/6/7
 */
class ScriptExecuteException(val script: Script, override val cause: Throwable) : RuntimeException() {

    override val message: String = asLang("module-script-exception-script-execute-failure", script.id, cause.localizedMessage)

}
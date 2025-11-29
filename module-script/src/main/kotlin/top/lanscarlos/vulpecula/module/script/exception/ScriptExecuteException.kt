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
class ScriptExecuteException(scriptId: String, override val cause: Throwable) :
    DefaultLocalizedException(Lang.MODULE_SCRIPT_EXECUTE_FAILURE, arrayOf(scriptId, cause.localizedMessage))
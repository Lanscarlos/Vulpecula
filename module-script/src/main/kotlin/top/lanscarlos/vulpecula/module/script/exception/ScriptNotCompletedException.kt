package top.lanscarlos.vulpecula.module.script.exception

import top.lanscarlos.vulpecula.common.exception.DefaultLocalizedException
import top.lanscarlos.vulpecula.common.lang.Lang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.script.exception
 *
 * @author Lanscarlos
 * @since 2025/11/24
 */
class ScriptNotCompletedException(scriptId: String) : DefaultLocalizedException(Lang.MODULE_SCRIPT_NOT_COMPLETED, arrayOf(scriptId))
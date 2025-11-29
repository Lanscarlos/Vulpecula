package top.lanscarlos.vulpecula.module.script.exception

import top.lanscarlos.vulpecula.common.exception.AbstractLocalizedException
import top.lanscarlos.vulpecula.common.exception.DefaultLocalizedException
import top.lanscarlos.vulpecula.common.lang.Lang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.script.exception
 *
 * @author Lanscarlos
 * @since 2025/5/6 14:20
 */
class ScriptNotFoundException(scriptId: String) : DefaultLocalizedException(Lang.MODULE_SCRIPT_NOT_FOUND, arrayOf(scriptId))
package top.lanscarlos.vulpecula.module.script.exception

import top.lanscarlos.vulpecula.common.exception.AbstractLocalizedException
import top.lanscarlos.vulpecula.common.lang.Lang
import top.lanscarlos.vulpecula.module.script.Script

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.script.exception
 *
 * @author Lanscarlos
 * @since 2025/11/24
 */
class ScriptNotCompletedException(val script: Script) : AbstractLocalizedException() {

    override val lang: Lang = Lang.EXCEPTION_SCRIPT_NOT_COMPLETED

    override val arguments: Array<Any> = arrayOf(script.id)

}
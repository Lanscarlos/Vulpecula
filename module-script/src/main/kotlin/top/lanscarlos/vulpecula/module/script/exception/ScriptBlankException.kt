package top.lanscarlos.vulpecula.module.script.exception

import top.lanscarlos.vulpecula.common.exception.AbstractLocalizedException
import top.lanscarlos.vulpecula.common.lang.Lang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.script.exception
 *
 * 脚本为空
 *
 * @author Lanscarlos
 * @since 2025/11/25
 */
class ScriptBlankException : AbstractLocalizedException() {

    override val lang: Lang = Lang.MODULE_SCRIPT_BLANK

    override val arguments: Array<Any> = arrayOf()

}
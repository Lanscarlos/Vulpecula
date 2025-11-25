package top.lanscarlos.vulpecula.common.exception

import top.lanscarlos.vulpecula.common.lang.Lang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.exception
 *
 * @author Lanscarlos
 * @since 2025/11/25
 */
class BlankStringException : AbstractLocalizedException() {

    override val lang: Lang = Lang.EXCEPTION_BLANK_STRING

    override val arguments: Array<Any> = arrayOf()

}
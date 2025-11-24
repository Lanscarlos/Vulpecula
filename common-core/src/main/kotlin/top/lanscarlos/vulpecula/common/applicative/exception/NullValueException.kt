package top.lanscarlos.vulpecula.common.applicative.exception

import top.lanscarlos.vulpecula.common.exception.AbstractLocalizedException
import top.lanscarlos.vulpecula.common.lang.Lang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.applicative.exception
 *
 * @author Lanscarlos
 * @since 2025/6/11
 */
class NullValueException : AbstractLocalizedException() {

    override val lang: Lang = Lang.EXCEPTION_CONVERT_NULL_VALUE

    override val arguments: Array<Any> = arrayOf()

}
package top.lanscarlos.vulpecula.common.applicative.exception

import top.lanscarlos.vulpecula.common.exception.AbstractLocalizedException
import top.lanscarlos.vulpecula.common.lang.Lang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.applicative.exception
 *
 * @author Lanscarlos
 * @since 2025/5/29 11:54
 */
class ValueConversionException(val value: String, val target: Class<*>) : AbstractLocalizedException() {

    override val lang: Lang = Lang.COMMON_APPLICATIVE_CONVERT_INVALID_VALUE

    override val arguments: Array<Any> = arrayOf(value, target.name)

}
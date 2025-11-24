package top.lanscarlos.vulpecula.common.applicative.exception

import top.lanscarlos.vulpecula.common.exception.AbstractLocalizedException
import top.lanscarlos.vulpecula.common.lang.Lang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.applicative.exception
 *
 * @author Lanscarlos
 * @since 2025/5/29 10:50
 */
class TypeConversionException(val source: Any, val targetClass: Class<*>) : AbstractLocalizedException() {

    val sourceClass: Class<*> = source.javaClass

    override val lang: Lang = Lang.EXCEPTION_CONVERT_UNSUPPORTED_TYPE

    override val arguments: Array<Any> = arrayOf(sourceClass.name, targetClass.name)

}
package top.lanscarlos.vulpecula.common.exception

import top.lanscarlos.vulpecula.common.lang.Lang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.exception
 *
 * @author Lanscarlos
 * @since 2025/11/25
 */
class UnsupportedValueException(value: Any) : AbstractLocalizedException() {

    override val lang: Lang = Lang.EXCEPTION_UNSUPPORTED_VALUE

    override val arguments: Array<Any> = arrayOf(value)

}
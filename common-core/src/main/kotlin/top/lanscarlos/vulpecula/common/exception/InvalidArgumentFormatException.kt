package top.lanscarlos.vulpecula.common.exception

import top.lanscarlos.vulpecula.common.lang.Lang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.core.exception
 *
 * @author Lanscarlos
 * @since 2025/6/9 11:07
 */
class InvalidArgumentFormatException(val value: String) : DefaultLocalizedException(
    lang = Lang.COMMON_EXCEPTION_INVALID_ARGUMENT_FORMAT,
    arguments = arrayOf(value)
)
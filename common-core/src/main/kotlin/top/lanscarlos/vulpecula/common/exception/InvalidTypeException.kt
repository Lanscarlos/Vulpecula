package top.lanscarlos.vulpecula.common.exception

import top.lanscarlos.vulpecula.common.lang.Lang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.core.exception
 *
 * @author Lanscarlos
 * @since 2025/6/4 10:52
 */
class InvalidTypeException(value: Any) : DefaultLocalizedException(
    lang = Lang.COMMON_EXCEPTION_INVALID_TYPE,
    arguments = arrayOf(value)
)
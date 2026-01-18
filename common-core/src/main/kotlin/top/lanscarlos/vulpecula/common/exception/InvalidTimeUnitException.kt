package top.lanscarlos.vulpecula.common.exception

import top.lanscarlos.vulpecula.common.lang.Lang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.core.exception
 *
 * @author Lanscarlos
 * @since 2025/6/4 11:08
 */
class InvalidTimeUnitException(value: String) : DefaultLocalizedException(
    lang = Lang.COMMON_EXCEPTION_INVALID_TIME_UNIT,
    arguments = arrayOf(value)
)
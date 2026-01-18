package top.lanscarlos.vulpecula.common.exception

import top.lanscarlos.vulpecula.common.lang.Lang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.core.exception
 *
 * @author Lanscarlos
 * @since 2025/6/9 13:16
 */
class WorldNotFoundException(val name: String) : DefaultLocalizedException(
    lang = Lang.COMMON_EXCEPTION_WORLD_NOT_FOUND,
    arguments = arrayOf(name)
)
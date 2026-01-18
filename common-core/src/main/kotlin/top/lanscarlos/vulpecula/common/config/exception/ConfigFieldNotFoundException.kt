package top.lanscarlos.vulpecula.common.config.exception

import top.lanscarlos.vulpecula.common.exception.AbstractLocalizedException
import top.lanscarlos.vulpecula.common.lang.Lang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.config.exception
 *
 * @author Lanscarlos
 * @since 2025/6/7
 */
class ConfigFieldNotFoundException(val field: String) : AbstractLocalizedException() {

    override val lang: Lang = Lang.COMMON_CONFIG_FIELD_NOT_FOUND

    override val arguments: Array<Any> = arrayOf(field)

}
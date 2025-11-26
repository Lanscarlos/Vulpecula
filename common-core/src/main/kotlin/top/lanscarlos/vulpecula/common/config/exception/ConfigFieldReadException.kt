package top.lanscarlos.vulpecula.common.config.exception

import top.lanscarlos.vulpecula.common.exception.AbstractLocalizedException
import top.lanscarlos.vulpecula.common.lang.Lang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.config
 *
 * 配置字段读取异常
 *
 * @author Lanscarlos
 * @since 2025/5/29 9:36
 */
class ConfigFieldReadException(val field: String, override val cause: Throwable) : AbstractLocalizedException() {

    override val lang: Lang = Lang.EXCEPTION_UNSUPPORTED_VALUE

    override val arguments: Array<Any> by lazy { arrayOf(field, cause.localizedMessage) }

}
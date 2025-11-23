package top.lanscarlos.vulpecula.common.config.exception

import top.lanscarlos.vulpecula.common.utils.asLang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.config
 *
 * 配置字段读取异常
 *
 * @author Lanscarlos
 * @since 2025/5/29 9:36
 */
class ConfigFieldReadException(val field: String, override val cause: Throwable) : RuntimeException() {

    override val message: String = asLang("common-config-exception-field-read-failure", field, cause.localizedMessage)

}